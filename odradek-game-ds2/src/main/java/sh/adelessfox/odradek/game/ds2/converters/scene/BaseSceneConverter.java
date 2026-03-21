package sh.adelessfox.odradek.game.ds2.converters.scene;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.game.ds2.rtti.data.ref.Ref;
import sh.adelessfox.odradek.geometry.*;
import sh.adelessfox.odradek.math.Matrix4f;
import sh.adelessfox.odradek.math.Vector3f;
import sh.adelessfox.odradek.scene.Scene;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

abstract class BaseSceneConverter<T> implements Converter<T, Scene, DS2Game> {
    private static final Logger log = LoggerFactory.getLogger(BaseSceneConverter.class);

    static Mesh convertMesh(
        List<Ref<DS2.ShadingGroup>> shadingGroups,
        List<Ref<DS2.PrimitiveResource>> primitiveResources,
        DS2.StreamingDataSource dataSource,
        DS2Game game
    ) {
        var buffer = ByteBuffer.wrap(game.readDataSource(dataSource)).order(ByteOrder.LITTLE_ENDIAN);
        var primitives = new ArrayList<Primitive>(primitiveResources.size());

        assert shadingGroups.size() == primitiveResources.size();

        for (var primitive : Ref.unwrap(primitiveResources)) {
            if (primitive.startIndex() > 0) {
                throw new UnsupportedOperationException("startIndex > 0 is not supported");
            }

            var vertexArray = primitive.vertexArray().get();
            var vertexAccessors = buildVertexAccessors(vertexArray, buffer);

            var indexArray = primitive.indexArray().get();
            var indexAccessor = buildIndexAccessor(indexArray, buffer, primitive.startIndex(), primitive.endIndex());

            primitives.add(new Primitive(
                indexAccessor,
                vertexAccessors,
                computePrimitiveColor(primitive.hash() ^ primitive.hashCode())));
        }

        if (buffer.hasRemaining()) {
            throw new IllegalStateException("Not all data was read from the buffer");
        }

        return Mesh.of(primitives);
    }

    static Matrix4f toMat4(DS2.Mat34 matrix) {
        return new Matrix4f(
            matrix.row0().x(), matrix.row1().x(), matrix.row2().x(), 0.f,
            matrix.row0().y(), matrix.row1().y(), matrix.row2().y(), 0.f,
            matrix.row0().z(), matrix.row1().z(), matrix.row2().z(), 0.f,
            matrix.row0().w(), matrix.row1().w(), matrix.row2().w(), 1.f
        );
    }

    static Matrix4f toMat4(DS2.WorldTransform transform) {
        var rot = transform.orientation();
        var pos = transform.position();
        return new Matrix4f(
            rot.col0().x(), rot.col0().y(), rot.col0().z(), 0.f,
            rot.col1().x(), rot.col1().y(), rot.col1().z(), 0.f,
            rot.col2().x(), rot.col2().y(), rot.col2().z(), 0.f,
            (float) pos.x(), (float) pos.y(), (float) pos.z(), 1.f
        );
    }

    private static Map<Semantic, Accessor> buildVertexAccessors(
        DS2.VertexArrayResource object,
        ByteBuffer buffer
    ) {
        var accessors = new HashMap<Semantic, Accessor>();
        var interleaved = new HashMap<Semantic, List<Accessor>>();
        var count = object.count();

        for (var stream : object.streams()) {
            var stride = stream.stride();

            ByteBuffer view;
            if (object.isStreaming()) {
                view = readBuffer(buffer, count, stride);
            } else {
                view = ByteBuffer
                    .wrap(stream.data())
                    .order(ByteOrder.LITTLE_ENDIAN);
            }

            for (var element : stream.elements()) {
                int offset = Byte.toUnsignedInt(element.offset());
                int components = element.slotsUsed();

                var type = switch (element.storageType().unwrap()) {
                    case UnsignedByte -> new Type.I8(components, true, false);
                    case UnsignedByteNormalized -> new Type.I8(components, true, true);
                    case SignedShort -> new Type.I16(components, false, false);
                    case SignedShortNormalized -> new Type.I16(components, false, true);
                    case UnsignedShort -> new Type.I16(components, true, false);
                    case UnsignedShortNormalized -> new Type.I16(components, true, true);
                    case HalfFloat -> new Type.F16(components);
                    case Float -> new Type.F32(components);
                    case X10Y10Z10W2Normalized -> new Type.X10Y10Z10W2(components, false, true);
                    case X10Y10Z10W2UNorm -> new Type.X10Y10Z10W2(components, true, true);
                    default -> {
                        log.warn(
                            "Skipping unsupported element (semantic: {}, format: {})",
                            element.element(),
                            element.storageType());
                        yield null;
                    }
                };

                if (type == null) {
                    continue;
                }

                var accessor = Accessor.of(view, offset, stride, type, count);
                var semantic = switch (element.element().unwrap()) {
                    case Pos -> Semantic.POSITION;
                    case TangentBFlip -> Semantic.TANGENT_BFLIP;
                    case Tangent -> Semantic.TANGENT;
                    case Binormal -> Semantic.BINORMAL;
                    case Normal -> Semantic.NORMAL;
                    case Color -> Semantic.COLOR;
                    case UV0 -> Semantic.TEXTURE_0;
                    case UV1 -> Semantic.TEXTURE_1;
                    case UV2 -> Semantic.TEXTURE_2;
                    case BlendWeights, BlendWeights2, BlendWeights3 -> {
                        // NOTE elements are expected to be ordered
                        interleaved.computeIfAbsent(Semantic.WEIGHTS, _ -> new ArrayList<>()).add(accessor);
                        yield null;
                    }
                    case BlendIndices, BlendIndices2, BlendIndices3 -> {
                        // NOTE elements are expected to be ordered
                        interleaved.computeIfAbsent(Semantic.JOINTS, _ -> new ArrayList<>()).add(accessor);
                        yield null;
                    }
                    default -> {
                        log.warn("Skipping unsupported element (semantic: {})", element.element());
                        yield null;
                    }
                };

                if (semantic != null) {
                    accessors.put(semantic, accessor);
                }
            }
        }

        interleaved.forEach(((semantic, value) -> {
            if (!value.isEmpty()) {
                accessors.put(semantic, Accessor.ofInterleaved(value));
            }
        }));

        if (accessors.containsKey(Semantic.JOINTS) && !accessors.containsKey(Semantic.WEIGHTS)) {
            // Weights MAY be absent in case there's only one bone per vertex.
            var joints = accessors.get(Semantic.JOINTS);
            if (joints.componentCount() != 1) {
                throw new IllegalStateException("JOINTS accessor has " + joints.componentCount()
                    + " components, but WEIGHTS accessor is missing");
            }

            // Build a dummy WEIGHTS accessor with all weights set to 1.0f
            var weights = ByteBuffer.allocate(Float.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(1.0f)
                .flip();
            var accessor = Accessor.of(weights, 0, 0, new Type.F32(1), joints.count());
            accessors.put(Semantic.WEIGHTS, accessor);
        }

        return accessors;
    }

    private static Accessor buildIndexAccessor(
        DS2.IndexArrayResource object,
        ByteBuffer buffer,
        int startIndex,
        int endIndex
    ) {
        var type = switch (object.format().unwrap()) {
            case Index16 -> new Type.I16(1, true, false);
            case Index32 -> new Type.I32(1, true, false);
        };

        var count = object.count();
        var stride = object.format().unwrap().stride();

        ByteBuffer view;
        if (object.isStreaming()) {
            view = readBuffer(buffer, count, stride);
        } else {
            view = ByteBuffer
                .wrap(object.data())
                .order(ByteOrder.LITTLE_ENDIAN);
        }

        return Accessor.of(view, startIndex * stride, stride, type, endIndex - startIndex);
    }

    private static Vector3f computePrimitiveColor(int hash) {
        var random = new Random(hash);
        return new Vector3f(
            random.nextFloat(0.5f, 1.0f),
            random.nextFloat(0.5f, 1.0f),
            random.nextFloat(0.5f, 1.0f)
        );
    }

    private static ByteBuffer readBuffer(ByteBuffer buffer, int count, int stride) {
        int position = align(buffer.position(), stride);
        int size = count * stride;
        var view = buffer
            .slice(position, size)
            .order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(position + size);
        return view;
    }

    private static int lcm(int c) {
        int a = c;
        int b = 16;
        while (b != 0) {
            int t = b;
            b = a % b;
            a = t;
        }
        return (c << 4) / a;
    }

    private static int align(int value, int granularity) {
        int target = lcm(granularity);
        return Math.ceilDiv(value, target) * target;
    }
}
