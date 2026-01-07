package sh.adelessfox.odradek.viewer.model.viewport.renderpass;

import sh.adelessfox.odradek.geometry.Primitive;
import sh.adelessfox.odradek.math.Matrix4f;
import sh.adelessfox.odradek.math.Vector3f;
import sh.adelessfox.odradek.scene.Node;
import sh.adelessfox.odradek.scene.Scene;
import sh.adelessfox.odradek.viewer.model.viewport.Camera;
import sh.adelessfox.odradek.viewer.model.viewport.Viewport;
import sh.adelessfox.odradek.viewer.model.viewport.ViewportInput;

import java.awt.event.KeyEvent;
import java.io.IOException;

public class OverlayRenderPass implements RenderPass {
    private static final String STATISTICS_FORMAT = """
        Statistics:
          Vertices %,d
          Faces    %,d
          Meshes   %,d
        
        Position:
          X % f
          Y % f
          Z % f
        
        Keybinds:
          [1] - Show skins [%c]
          [2] - Show bounding boxes [%c]
        """;

    private DebugRenderer debug;
    private Scene scene;
    private SceneStatistics statistics;

    private boolean showSkins;
    private boolean showBoundingBoxes;

    @Override
    public void init() throws IOException {
        debug = new DebugRenderer();
    }

    @Override
    public void dispose() {
        if (debug != null) {
            debug.dispose();
        }
    }

    @Override
    public void process(Viewport viewport, double dt, ViewportInput input) {
        if (input.isKeyPressed(KeyEvent.VK_1)) {
            showSkins ^= true;
        }
        if (input.isKeyPressed(KeyEvent.VK_2)) {
            showBoundingBoxes ^= true;
        }
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
                renderNode(node, node.matrix(), camera);
            }

            renderInformation(statistics, camera);
        }

        if (viewport.isCameraOriginShown()) {
            debug.cross(viewport.getCameraOrigin(), 0.1f, false);
        }

        debug.draw(viewport, dt);
    }

    private void renderInformation(SceneStatistics statistics, Camera camera) {
        var position = camera.position();
        var text = STATISTICS_FORMAT.formatted(
            statistics.vertices,
            statistics.faces,
            statistics.meshes,
            position.x(), position.y(), position.z(),
            showSkins ? 'X' : ' ',
            showBoundingBoxes ? 'X' : ' '
        );
        debug.billboardText(text, 10, 10, 1.0f, 1.0f, 1.0f, 12.0f);
    }

    private void renderNode(Node node, Matrix4f transform, Camera camera) {
        for (Node child : node.children()) {
            renderNode(child, transform.mul(child.matrix()), camera);
        }
        if (showSkins) {
            node.skin().ifPresent(skin -> renderSkin(skin, transform.mul(skin.matrix()), camera));
        }
        if (showBoundingBoxes) {
            node.mesh().ifPresent(mesh -> debug.aabb(mesh.computeBoundingBox().transform(transform), Vector3f.one()));
        }
    }

    private Vector3f renderSkin(Node node, Matrix4f transform, Camera camera) {
        var source = transform.toTranslation();

        node.name().ifPresent(name -> {
            var distance = source.distance(camera.position());
            var size = Math.clamp(16.0f / distance, 4.0f, 16.0f);
            debug.projectedText(name, source, camera, new Vector3f(1, 1, 1), size);
        });

        for (Node child : node.children()) {
            var target = renderSkin(child, transform.mul(child.matrix()), camera);
            debug.line(target, source, new Vector3f(0, 1, 0), false);
        }

        debug.point(source, new Vector3f(1, 0, 1), 10f, false);

        return source;
    }

    private static SceneStatistics summarizeScene(Scene scene) {
        var statistics = new SceneStatistics();
        for (Node node : scene.nodes()) {
            accumulateNodeStatistics(node, statistics);
        }
        return statistics;
    }

    private static void accumulateNodeStatistics(Node node, SceneStatistics statistics) {
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
