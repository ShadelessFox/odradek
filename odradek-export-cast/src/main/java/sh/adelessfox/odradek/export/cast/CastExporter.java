package sh.adelessfox.odradek.export.cast;

import be.twofold.tinycast.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.geometry.*;
import sh.adelessfox.odradek.math.Matrix4f;
import sh.adelessfox.odradek.scene.Node;
import sh.adelessfox.odradek.scene.Scene;

import java.io.IOException;
import java.nio.*;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Optional;
import java.util.OptionalInt;

public class CastExporter implements Exporter<Scene> {
    private static final Logger log = LoggerFactory.getLogger(CastExporter.class);

    @Override
    public void export(Scene object, WritableByteChannel channel) throws IOException {
        var cast = Cast.create();
        var root = cast.createRoot();

        for (Node node : object.nodes()) {
            exportNode(root, node, Matrix4f.identity());
        }

        try {
            cast.write(Channels.newOutputStream(channel));
        } catch (CastException e) {
            throw new IOException("Error writing cast file", e);
        }
    }

    @Override
    public String id() {
        return "cast";
    }

    @Override
    public String name() {
        return "Cast (by DTZxPorter)";
    }

    @Override
    public String extension() {
        return "cast";
    }

    @Override
    public Optional<String> icon() {
        return Optional.of("fugue:paint-can");
    }

    private static void exportNode(CastNodes.Root root, Node node, Matrix4f transform) {
        node.mesh().ifPresent(mesh -> {
            var pos = transform.toTranslation();
            var rot = transform.toRotation();
            var scl = transform.toScale();

            var model = root.createModel();
            model.setPosition(new Vec3(pos.x(), pos.y(), pos.z()));
            model.setRotation(new Vec4(rot.x(), rot.y(), rot.z(), rot.w()));
            model.setScale(new Vec3(scl.x(), scl.y(), scl.z()));
            node.name().ifPresent(model::setName);

            exportMesh(model, mesh);
            node.skin().ifPresent(skin -> exportSkeleton(model, skin));
        });

        for (Node child : node.children()) {
            exportNode(root, child, transform.mul(child.matrix()));
        }
    }

    private static void exportMesh(CastNodes.Model model, Mesh nodeMesh) {
        for (Primitive primitive : nodeMesh.primitives()) {
            var result = model.createMesh();
            var joints = new ArrayList<Accessor>(4);
            var weights = new ArrayList<Accessor>(4);

            // Vertices
            primitive.vertices().forEach((semantic, accessor) -> {
                switch (semantic) {
                    case Semantic.Position _ -> result.setVertexPositionBuffer(toFloatBuffer(accessor));
                    case Semantic.Normal _ -> result.setVertexNormalBuffer(toFloatBuffer(accessor));
                    case Semantic.Joints(int n) -> {
                        while (joints.size() <= n) {
                            joints.add(null);
                        }
                        joints.set(n, accessor);
                    }
                    case Semantic.Weights(int n) -> {
                        while (weights.size() <= n) {
                            weights.add(null);
                        }
                        weights.set(n, accessor);
                    }
                    case Semantic.Texture _ -> {
                        result.addVertexUVBuffer(toFloatBuffer(accessor));
                        result.setUVLayerCount(result.getUVLayerCount().orElse(0) + 1);
                    }
                    case Semantic.Color _ -> {
                        var buffer = IntBuffer.allocate(accessor.count());
                        var view = accessor.asByteView();
                        for (int i = 0; i < accessor.count(); i++) {
                            int r = Byte.toUnsignedInt(view.get(i, 0));
                            int g = Byte.toUnsignedInt(view.get(i, 1));
                            int b = Byte.toUnsignedInt(view.get(i, 2));
                            int a = Byte.toUnsignedInt(view.get(i, 3));
                            buffer.put((a << 24) | (b << 16) | (g << 8) | r);
                        }
                        result.addVertexColorBufferI32(buffer.flip());
                        result.setColorLayerCount(result.getColorLayerCount().orElse(0) + 1);
                    }
                    default -> log.debug("Skipping unsupported vertex {}", semantic);
                }
            });

            // Weights
            var jointsAccessor = Accessor.ofInterleaved(joints);
            var weightsAccessor = Accessor.ofInterleaved(weights);
            if (jointsAccessor.count() != weightsAccessor.count() || jointsAccessor.componentCount() != weightsAccessor.componentCount()) {
                log.error("Joints and weights accessors do not match! Skipping skinning data");
                continue;
            }

            result.setMaximumWeightInfluence(jointsAccessor.componentCount());
            result.setVertexWeightBoneBuffer(toBuffer(jointsAccessor));
            result.setVertexWeightValueBuffer(toFloatBuffer(weightsAccessor));

            // Indices
            result.setFaceBuffer(toBuffer(primitive.indices()));
        }
    }

    private static void exportSkeleton(CastNodes.Model model, Node nodeSkin) {
        var skeleton = model.createSkeleton();
        exportBone(skeleton, OptionalInt.empty(), nodeSkin);
    }

    private static void exportBone(CastNodes.Skeleton skeleton, OptionalInt parent, Node node) {
        var bone = skeleton.createBone();
        node.name().ifPresent(bone::setName);
        parent.ifPresent(bone::setParentIndex);

        var transform = node.matrix();
        var pos = transform.toTranslation();
        var rot = transform.toRotation();
        var scl = transform.toScale();

        bone.setLocalPosition(new Vec3(pos.x(), pos.y(), pos.z()));
        bone.setLocalRotation(new Vec4(rot.x(), rot.y(), rot.z(), rot.w()));
        bone.setScale(new Vec3(scl.x(), scl.y(), scl.z()));

        int index = skeleton.getBones().size() - 1;
        for (Node child : node.children()) {
            exportBone(skeleton, OptionalInt.of(index), child);
        }
    }

    // region Buffers
    private static Buffer toBuffer(Accessor accessor) {
        return switch (accessor.type()) {
            case Type.I8 _ -> toByteBuffer(accessor);
            case Type.I16 _ -> toShortBuffer(accessor);
            case Type.I32 _ -> toIntBuffer(accessor);
            default -> throw new IllegalArgumentException("Unsupported component type: " + accessor.type());
        };
    }

    private static ByteBuffer toByteBuffer(Accessor accessor) {
        var buffer = ByteBuffer.allocate(accessor.count() * accessor.componentCount());
        var view = accessor.asByteView();
        for (int i = 0; i < accessor.count(); i++) {
            for (int j = 0; j < accessor.componentCount(); j++) {
                buffer.put(view.get(i, j));
            }
        }
        return buffer.flip();
    }

    private static ShortBuffer toShortBuffer(Accessor accessor) {
        var buffer = ShortBuffer.allocate(accessor.count() * accessor.componentCount());
        var view = accessor.asShortView();
        for (int i = 0; i < accessor.count(); i++) {
            for (int j = 0; j < accessor.componentCount(); j++) {
                buffer.put(view.get(i, j));
            }
        }
        return buffer.flip();
    }

    private static IntBuffer toIntBuffer(Accessor accessor) {
        var buffer = IntBuffer.allocate(accessor.count() * accessor.componentCount());
        var view = accessor.asIntView();
        for (int i = 0; i < accessor.count(); i++) {
            for (int j = 0; j < accessor.componentCount(); j++) {
                buffer.put(view.get(i, j));
            }
        }
        return buffer.flip();
    }

    private static FloatBuffer toFloatBuffer(Accessor accessor) {
        var buffer = FloatBuffer.allocate(accessor.count() * accessor.componentCount());
        var view = accessor.asFloatView();
        for (int i = 0; i < accessor.count(); i++) {
            for (int j = 0; j < accessor.componentCount(); j++) {
                buffer.put(view.get(i, j));
            }
        }
        return buffer.flip();
    }
    // endregion
}
