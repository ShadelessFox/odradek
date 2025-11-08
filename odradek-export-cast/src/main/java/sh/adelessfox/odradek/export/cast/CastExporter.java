package sh.adelessfox.odradek.export.cast;

import be.twofold.tinycast.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.geometry.ComponentType;
import sh.adelessfox.odradek.geometry.Mesh;
import sh.adelessfox.odradek.geometry.Primitive;
import sh.adelessfox.odradek.geometry.Semantic;
import sh.adelessfox.odradek.math.Matrix4f;
import sh.adelessfox.odradek.scene.Node;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Optional;

public class CastExporter implements Exporter<Node> {
    private static final Logger log = LoggerFactory.getLogger(CastExporter.class);

    @Override
    public void export(Node object, WritableByteChannel channel) throws IOException {
        var cast = Cast.create();
        var root = cast.createRoot();

        exportNode(root, object, Matrix4f.identity());

        try {
            cast.write(Channels.newOutputStream(channel));
        } catch (CastException e) {
            throw new IOException("Error writing cast file", e);
        }
    }

    @Override
    public Optional<String> icon() {
        return Optional.of("fugue:paint-can");
    }

    @Override
    public String name() {
        return "Cast (by DTZxPorter)";
    }

    @Override
    public String extension() {
        return "cast";
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
        });

        for (Node child : node.children()) {
            exportNode(root, child, transform.mul(child.matrix()));
        }
    }

    private static void exportMesh(CastNodes.Model model, Mesh nodeMesh) {
        for (Primitive primitive : nodeMesh.primitives()) {
            var result = model.createMesh();

            // Vertices
            primitive.vertices().forEach((semantic, accessor) -> {
                switch (semantic) {
                    case Semantic.Position _,
                         Semantic.Normal _ -> {
                        var buffer = FloatBuffer.allocate(accessor.count() * 3);
                        var view = accessor.asFloatView();
                        for (int i = 0; i < accessor.count(); i++) {
                            buffer.put(view.get(i, 0));
                            buffer.put(view.get(i, 1));
                            buffer.put(view.get(i, 2));
                        }
                        if (semantic instanceof Semantic.Position) {
                            result.setVertexPositionBuffer(buffer.flip());
                        } else {
                            result.setVertexNormalBuffer(buffer.flip());
                        }
                    }
                    case Semantic.Color(var n) when n == 0 -> {
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
                    }
                    default -> log.debug("Skipping unsupported vertex {}", semantic);
                }
            });

            // Indices
            var indices = primitive.indices();
            if (indices.componentType() == ComponentType.UNSIGNED_SHORT) {
                var buffer = ShortBuffer.allocate(indices.count());
                var view = indices.asShortView();
                for (int i = 0; i < indices.count(); i++) {
                    buffer.put(view.get(i, 0));
                }
                result.setFaceBuffer(buffer.flip());
            } else {
                var buffer = IntBuffer.allocate(indices.count());
                var view = indices.asIntView();
                for (int i = 0; i < indices.count(); i++) {
                    buffer.put(view.get(i, 0));
                }
                result.setFaceBuffer(buffer.flip());
            }
        }
    }
}
