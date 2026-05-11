package sh.adelessfox.odradek.export.cast;

import be.twofold.tinycast.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.geometry.Mesh;
import sh.adelessfox.odradek.geometry.Model;
import sh.adelessfox.odradek.scene.Joint;
import sh.adelessfox.odradek.scene.Node;
import sh.adelessfox.odradek.scene.Scene;
import sh.adelessfox.odradek.scene.Skin;
import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.collect.Floats;
import wtf.reversed.toolbox.math.Matrix4;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Optional;
import java.util.OptionalInt;

public class CastExporter implements Exporter.OfSingleOutput<Scene> {
    private static final Logger log = LoggerFactory.getLogger(CastExporter.class);

    @Override
    public void export(Scene object, WritableByteChannel channel) throws IOException {
        var cast = Cast.create();
        var root = cast.createRoot();

        root.createMetadata()
            .setSoftware("Odradek")
            .setAuthor("ShadelessFox");

        for (Node node : object.nodes()) {
            exportNode(node, Matrix4.IDENTITY, root);
        }

        try {
            cast.write(Channels.newOutputStream(channel));
        } catch (CastException e) {
            throw new IOException("Error writing cast file", e);
        }
    }

    @Override
    public String id() {
        return "model.cast";
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

    private static void exportNode(Node node, Matrix4 transform, CastNodes.Root root) {
        node.model().ifPresent(m -> {
            var pos = transform.toTranslation();
            var rot = transform.toRotation();
            var scl = transform.toScale();

            var model = root.createModel();
            model.setPosition(new Vec3(pos.x(), pos.y(), pos.z()));
            model.setRotation(new Vec4(rot.x(), rot.y(), rot.z(), rot.w()));
            model.setScale(new Vec3(scl.x(), scl.y(), scl.z()));

            node.name().ifPresent(model::setName);
            node.skin().ifPresent(skin -> exportSkeleton(model, skin));

            exportModel(model, m);
        });

        for (Node child : node.children()) {
            exportNode(child, transform.multiply(child.matrix()), root);
        }
    }

    private static void exportModel(CastNodes.Model model, Model nodeModel) {
        for (Mesh mesh : nodeModel.meshes()) {
            var result = model.createMesh();
            result.setFaceBuffer(mesh.indices().asBuffer());
            result.setVertexPositionBuffer(mesh.positions().asBuffer());

            mesh.normals().ifPresent(normals -> result.setVertexNormalBuffer(normals.asBuffer()));
            mesh.tangents().ifPresent(tangents -> result.setVertexTangentBuffer(mapTangentBuffer(tangents.asBuffer())));

            for (Floats texCoords : mesh.texCoords()) {
                result.addVertexUVBuffer(mapUvBuffer(texCoords.asBuffer()));
                result.setUVLayerCount(result.getUVLayerCount().orElse(0) + 1);
            }

            for (Bytes colors : mesh.colors()) {
                result.addVertexColorBufferI32(mapColorBuffer(colors.asBuffer()));
                result.setColorLayerCount(result.getColorLayerCount().orElse(0) + 1);
            }

            mesh.weights().ifPresent(weights -> {
                result.setMaximumWeightInfluence(weights.maxInfluence());
                result.setVertexWeightBoneBuffer(weights.joints().asBuffer());
                result.setVertexWeightValueBuffer(weights.values().asBuffer());
            });
        }
    }

    private static void exportSkeleton(CastNodes.Model model, Skin nodeSkin) {
        var skeleton = model.createSkeleton();
        for (Joint joint : nodeSkin.joints()) {
            exportBone(skeleton, joint.parent(), joint);
        }
    }

    private static void exportBone(CastNodes.Skeleton skeleton, OptionalInt parent, Joint joint) {
        var bone = skeleton.createBone();
        bone.setName(joint.name());
        parent.ifPresent(bone::setParentIndex);

        var transform = joint.matrix();
        var pos = transform.toTranslation();
        var rot = transform.toRotation();
        var scl = transform.toScale();

        bone.setLocalPosition(new Vec3(pos.x(), pos.y(), pos.z()));
        bone.setLocalRotation(new Vec4(rot.x(), rot.y(), rot.z(), rot.w()));
        bone.setScale(new Vec3(scl.x(), scl.y(), scl.z()));
    }

    private static FloatBuffer mapTangentBuffer(FloatBuffer buffer) {
        var limit = buffer.limit();
        var output = FloatBuffer.allocate(limit * 3 / 4);
        for (int i = 0, o = 0; i < limit; i += 4, o += 3) {
            output.put(o/**/, buffer.get(i/**/));
            output.put(o + 1, buffer.get(i + 1));
            output.put(o + 2, buffer.get(i + 2));
        }
        return output.flip();
    }

    private static FloatBuffer mapUvBuffer(FloatBuffer buffer) {
        var limit = buffer.limit();
        var output = FloatBuffer.allocate(limit);
        for (int i = 0; i < buffer.limit(); i += 2) {
            float u = buffer.get(i/**/);
            float v = buffer.get(i + 1);
            output.put(u).put(1.0f - v);
        }
        return output.flip();
    }

    private static IntBuffer mapColorBuffer(ByteBuffer buffer) {
        return buffer.asIntBuffer();
    }
}
