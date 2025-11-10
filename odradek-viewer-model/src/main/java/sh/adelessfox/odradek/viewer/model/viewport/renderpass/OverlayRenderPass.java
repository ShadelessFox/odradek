package sh.adelessfox.odradek.viewer.model.viewport.renderpass;

import sh.adelessfox.odradek.geometry.Primitive;
import sh.adelessfox.odradek.scene.Node;
import sh.adelessfox.odradek.scene.Scene;
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
        if (scene != null) {
            if (this.scene != scene) {
                this.scene = scene;
                this.statistics = summarizeScene(scene);
            }

            var text = STATISTICS_FORMAT.format(new Object[]{
                statistics.vertices,
                statistics.faces,
                statistics.meshes
            });

            debug.billboardText(text, 10, 10, 1.0f, 1.0f, 1.0f, 14.0f);
        }

        if (viewport.isCameraOriginShown()) {
            debug.cross(viewport.getCameraOrigin(), 0.1f, false);
        }

        debug.draw(viewport, dt);
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
