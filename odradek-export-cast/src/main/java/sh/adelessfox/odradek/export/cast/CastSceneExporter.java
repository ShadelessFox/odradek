package sh.adelessfox.odradek.export.cast;

import be.twofold.tinycast.CastNodes;
import be.twofold.tinycast.Vec3;
import be.twofold.tinycast.Vec4;
import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.geometry.Mesh;
import sh.adelessfox.odradek.geometry.Model;
import sh.adelessfox.odradek.scene.Node;
import sh.adelessfox.odradek.scene.Scene;
import sh.adelessfox.odradek.scene.Skeleton;
import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.collect.Floats;
import wtf.reversed.toolbox.math.Matrix4;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class CastSceneExporter
    extends BaseCastExporter<Scene>
    implements Exporter.OfSingleOutput<Scene> {

    @Override
    protected void export(Scene object, CastNodes.Root root) {
        for (Node node : object.nodes()) {
            exportNode(node, Matrix4.IDENTITY, root, new HashMap<>());
        }
    }

    @Override
    public String id() {
        return "model.cast";
    }

    private static void exportNode(
        Node node,
        Matrix4 parentTransform,
        CastNodes.Root root,
        Map<SkeletonInstance, CastNodes.Model> models
    ) {
        var transform = parentTransform.multiply(node.matrix());
        var skeleton = node.skeleton().orElse(null);
        node.model().ifPresent(m -> {
            CastNodes.Model model;
            if (skeleton == null) {
                model = createModel(node, transform, null, root);
            } else {
                model = models.computeIfAbsent(
                    new SkeletonInstance(skeleton, transform),
                    _ -> createModel(node, transform, skeleton, root));
            }

            mapModel(model, m, node.name().or(m::name));
        });

        for (Node child : node.children()) {
            exportNode(child, transform, root, models);
        }
    }

    private static CastNodes.Model createModel(Node node, Matrix4 transform, Skeleton skeleton, CastNodes.Root root) {
        var trs = transform.decompose();
        var pos = trs.translation();
        var rot = trs.rotation();
        var scl = trs.scale();

        var model = root.createModel();
        model.setPosition(new Vec3(pos.x(), pos.y(), pos.z()));
        model.setRotation(new Vec4(rot.x(), rot.y(), rot.z(), rot.w()));
        model.setScale(new Vec3(scl.x(), scl.y(), scl.z()));
        node.name().ifPresent(model::setName);
        if (skeleton != null) {
            mapSkeleton(model.createSkeleton(), skeleton);
        }
        return model;
    }

    private static void mapModel(CastNodes.Model model, Model nodeModel, Optional<String> name) {
        for (Mesh mesh : nodeModel.meshes()) {
            var result = model.createMesh();
            name.ifPresent(result::setName);
            result.setFaceBuffer(mesh.indices().asBuffer());
            result.setVertexPositionBuffer(mesh.positions().asBuffer());

            mesh.normals().ifPresent(normals -> result.setVertexNormalBuffer(normals.asBuffer()));
            mesh.tangents().ifPresent(tangents -> result.setVertexTangentBuffer(tangents.asBuffer()));

            for (Floats texCoord : mesh.texCoords()) {
                result.addVertexUVBuffer(mapUvBuffer(texCoord.asBuffer()));
                result.setUVLayerCount(result.getUVLayerCount().orElse(0) + 1);
            }

            for (Bytes colors : mesh.colors()) {
                result.addVertexColorBufferI32(mapColorBuffer(colors.asBuffer()));
                result.setColorLayerCount(result.getColorLayerCount().orElse(0) + 1);
            }

            mesh.weights().ifPresent(weights -> {
                result.setMaximumWeightInfluence(weights.maxInfluence());
                result.setVertexWeightBoneBuffer(weights.bones().asBuffer());
                result.setVertexWeightValueBuffer(weights.values().asBuffer());
            });
        }
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

    private record SkeletonInstance(Skeleton skeleton, Matrix4 transform) {
    }
}
