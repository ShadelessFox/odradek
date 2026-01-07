package sh.adelessfox.odradek.game.hfw.converters.scene;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.NotImplementedException;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest;
import sh.adelessfox.odradek.geometry.*;
import sh.adelessfox.odradek.math.Matrix4f;
import sh.adelessfox.odradek.rtti.data.Ref;
import sh.adelessfox.odradek.scene.Scene;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class BaseSceneConverter<T> implements Converter<T, Scene, ForbiddenWestGame> {
    private static final Logger log = LoggerFactory.getLogger(BaseSceneConverter.class);

    static Mesh convertMesh(
        List<Ref<HorizonForbiddenWest.ShadingGroup>> shadingGroups,
        List<Ref<HorizonForbiddenWest.PrimitiveResource>> primitiveResources,
        HorizonForbiddenWest.StreamingDataSource dataSource,
        ForbiddenWestGame game
    ) {
        var buffer = ByteBuffer.wrap(game.readDataSource(dataSource)).order(ByteOrder.LITTLE_ENDIAN);
        var primitives = new ArrayList<Primitive>(primitiveResources.size());

        assert shadingGroups.size() == primitiveResources.size();

        for (int i = 0; i < shadingGroups.size(); i++) {
            var primitive = primitiveResources.get(i).get();

            if (primitive.startIndex() > 0) {
                throw new NotImplementedException();
            }

            var vertexArray = primitive.vertexArray().get();
            var vertexAccessors = buildVertexAccessors(vertexArray, buffer);

            var indexArray = primitive.indexArray().get();
            var indexAccessor = buildIndexAccessor(indexArray, buffer, primitive.startIndex(), primitive.endIndex());

            primitives.add(new Primitive(indexAccessor, vertexAccessors, primitive.hashCode()));
        }

        if (buffer.hasRemaining()) {
            throw new IllegalStateException("Not all data was read from the buffer");
        }

        return Mesh.of(primitives);
    }

    static Matrix4f toMat4(HorizonForbiddenWest.Mat34 matrix) {
        return new Matrix4f(
            matrix.row0().x(), matrix.row1().x(), matrix.row2().x(), 0.f,
            matrix.row0().y(), matrix.row1().y(), matrix.row2().y(), 0.f,
            matrix.row0().z(), matrix.row1().z(), matrix.row2().z(), 0.f,
            matrix.row0().w(), matrix.row1().w(), matrix.row2().w(), 1.f
        );
    }

    static Matrix4f toMat4(HorizonForbiddenWest.WorldTransform transform) {
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
        HorizonForbiddenWest.VertexArrayResource object,
        ByteBuffer buffer
    ) {
        var accessors = new HashMap<Semantic, Accessor>();
        var count = object.count();

        for (var stream : object.streams()) {
            var stride = stream.stride();

            ByteBuffer view;
            if (object.isStreaming()) {
                view = readBufferAligned(buffer, count, stride);
            } else {
                view = ByteBuffer
                    .wrap(stream.data())
                    .order(ByteOrder.LITTLE_ENDIAN);
            }

            for (var element : stream.elements()) {
                var offset = Byte.toUnsignedInt(element.offset());

                var semantic = switch (element.element().unwrap()) {
                    case Pos -> Semantic.POSITION;
                    case Tangent -> Semantic.TANGENT;
                    case Normal -> Semantic.NORMAL;
                    case Color -> Semantic.COLOR_0;
                    case UV0 -> Semantic.TEXTURE_0;
                    case UV1 -> Semantic.TEXTURE_1;
                    case BlendWeights -> Semantic.WEIGHTS_0;
                    case BlendWeights2 -> Semantic.WEIGHTS_1;
                    case BlendIndices -> Semantic.JOINTS_0;
                    case BlendIndices2 -> Semantic.JOINTS_1;
                    default -> {
                        log.warn("Skipping unsupported element (semantic: {})", element.element());
                        yield null;
                    }
                };

                if (semantic == null) {
                    continue;
                }

                var elementType = switch (element.slotsUsed()) {
                    case 1 -> ElementType.SCALAR;
                    case 2 -> ElementType.VEC2;
                    case 3 -> ElementType.VEC3;
                    case 4 -> ElementType.VEC4;
                    default -> {
                        log.warn("Skipping unsupported element (semantic: {}, size: {})", element.element(), element.slotsUsed());
                        yield null;
                    }
                };

                if (elementType == null) {
                    continue;
                }

                var accessor = switch (element.storageType().unwrap()) {
                    // @formatter:off
                    case UnsignedByte ->
                        new Accessor(view, elementType, ComponentType.UNSIGNED_BYTE, offset, count, stride, false);
                    case UnsignedByteNormalized ->
                        new Accessor(view, elementType, ComponentType.UNSIGNED_BYTE, offset, count, stride, true);
                    case UnsignedShort ->
                        new Accessor(view, elementType, ComponentType.UNSIGNED_SHORT, offset, count, stride, false);
                    case UnsignedShortNormalized ->
                        new Accessor(view, elementType, ComponentType.UNSIGNED_SHORT, offset, count, stride, true);
                    case SignedShort ->
                        new Accessor(view, elementType, ComponentType.SHORT, offset, count, stride, false);
                    case SignedShortNormalized ->
                        new Accessor(view, elementType, ComponentType.SHORT, offset, count, stride, true);
                    case HalfFloat ->
                        new Accessor(view, elementType, ComponentType.HALF_FLOAT, offset, count, stride, false);
                    case Float ->
                        new Accessor(view, elementType, ComponentType.FLOAT, offset, count, stride, false);
                    default -> {
                        log.warn("Skipping unsupported element (semantic: {}, format: {})", element.element(), element.storageType());
                        yield null;
                    }
                    // @formatter:on
                };

                if (accessor != null) {
                    accessors.put(semantic, accessor);
                }
            }
        }

        return accessors;
    }

    private static Accessor buildIndexAccessor(
        HorizonForbiddenWest.IndexArrayResource object,
        ByteBuffer buffer,
        int startIndex,
        int endIndex
    ) {
        var component = switch (object.format().unwrap()) {
            case Index16 -> ComponentType.UNSIGNED_SHORT;
            case Index32 -> ComponentType.UNSIGNED_INT;
        };

        var count = object.count();
        var stride = object.format().unwrap().stride();

        ByteBuffer view;
        if (object.isStreaming()) {
            view = readBufferAligned(buffer, count, stride);
        } else {
            view = ByteBuffer
                .wrap(object.data())
                .order(ByteOrder.LITTLE_ENDIAN);
        }

        return new Accessor(view, ElementType.SCALAR, component, startIndex * stride, endIndex - startIndex, stride);
    }

    private static ByteBuffer readBufferAligned(ByteBuffer buffer, int count, int stride) {
        int position = alignUp(buffer.position(), stride);
        int size = count * stride;
        var view = buffer
            .slice(position, size)
            .order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(position + size);
        return view;
    }

    private static int alignUp(int value, int alignment) {
        return Math.ceilDiv(value, alignment) * alignment;
    }
}
