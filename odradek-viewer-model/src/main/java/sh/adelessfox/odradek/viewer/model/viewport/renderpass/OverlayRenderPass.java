package sh.adelessfox.odradek.viewer.model.viewport.renderpass;

import sh.adelessfox.odradek.geometry.Primitive;
import sh.adelessfox.odradek.math.Matrix4f;
import sh.adelessfox.odradek.math.Vector3f;
import sh.adelessfox.odradek.scene.Node;
import sh.adelessfox.odradek.scene.Scene;
import sh.adelessfox.odradek.viewer.model.viewport.Camera;
import sh.adelessfox.odradek.viewer.model.viewport.Viewport;

import java.text.MessageFormat;

public class OverlayRenderPass implements RenderPass {
    private static final MessageFormat STATISTICS_FORMAT = new MessageFormat("""
        Vertices {0,number,integer}
        Faces    {1,number,integer}
        Meshes   {2,number,integer}
        """);

    private DebugRenderPass debug;
    private Scene scene;
    private SceneStatistics statistics;

    @Override
    public void init() {
        debug = new DebugRenderPass();
        debug.init();
    }

    @Override
    public void dispose() {
        debug.dispose();
    }

    @Override
    public void draw(Viewport viewport, double dt) {
        var scene = viewport.getScene();
        var camera = viewport.getCamera();
        if (scene != null && camera != null) {
            if (this.scene != scene) {
                this.scene = scene;
                this.statistics = summarizeScene(scene);
            }

            for (Node node : scene.nodes()) {
                renderNode(node, Matrix4f.identity(), camera);
            }

            renderStatistics(statistics);
        }

        if (viewport.isCameraOriginShown()) {
            debug.cross(viewport.getCameraOrigin(), 0.1f, false);
        }

        debug.draw(viewport, dt);
    }

    private void renderStatistics(SceneStatistics statistics) {
        var text = STATISTICS_FORMAT.format(new Object[]{
            statistics.vertices,
            statistics.faces,
            statistics.meshes
        });

        debug.billboardText(text, 10, 10, 1.0f, 1.0f, 1.0f, 14.0f);
    }

    private void renderNode(Node node, Matrix4f transform, Camera camera) {
        for (Node child : node.children()) {
            renderNode(child, transform.mul(child.matrix()), camera);
        }

        if (node.skin().isPresent()) {
            renderSkin(node.skin().get(), null, camera);
        }
    }

    private void renderSkin(Node node, Node parent, Camera camera) {
        if (parent != null) {
            var translation = node.matrix().toTranslation();
            debug.point(translation, new Vector3f(1, 0, 1), 10f, false);
            debug.line(parent.matrix().toTranslation(), translation, new Vector3f(0, 1, 0), false);

            if (node.name().isPresent()) {
                var distance = translation.distance(camera.position());
                var size = Math.clamp(16.0f / distance, 4.0f, 16.0f);
                debug.projectedText(node.name().get(), translation, camera.projectionView(), new Vector3f(1, 1, 1), size);
            }
        }

        for (Node child : node.children()) {
            renderSkin(child, node, camera);
        }
    }

    private SceneStatistics summarizeScene(Scene scene) {
        var statistics = new SceneStatistics();
        for (Node node : scene.nodes()) {
            accumulateNodeStatistics(node, statistics);
        }
        return statistics;
    }

    private void accumulateNodeStatistics(Node node, SceneStatistics statistics) {
        var mesh = node.mesh().orElse(null);
        if (mesh != null) {
            for (Primitive primitive : mesh.primitives()) {
                statistics.vertices += primitive.positions().count();
                statistics.faces += primitive.indices().count() / 3;
            }
            statistics.meshes += 1;
        }
        for (Node child : node.children()) {
            accumulateNodeStatistics(child, statistics);
        }
    }

    private static final class SceneStatistics {
        private long vertices;
        private long faces;
        private int meshes;
    }
}
