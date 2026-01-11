package sh.adelessfox.odradek.viewer.model.viewport.renderpass;

import sh.adelessfox.odradek.geometry.Mesh;
import sh.adelessfox.odradek.geometry.Primitive;
import sh.adelessfox.odradek.math.Matrix4f;
import sh.adelessfox.odradek.math.Vector3f;
import sh.adelessfox.odradek.scene.Node;
import sh.adelessfox.odradek.scene.Scene;
import sh.adelessfox.odradek.viewer.model.viewport.Camera;
import sh.adelessfox.odradek.viewer.model.viewport.Viewport;
import sh.adelessfox.odradek.viewer.model.viewport.ViewportContext;
import sh.adelessfox.odradek.viewer.model.viewport.ViewportInput;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class OverlayRenderPass implements RenderPass {
    private static final List<Toggle> toggles = List.of(
        new Toggle("Show wireframe", KeyEvent.VK_1,
            ViewportContext::isShowWireframe,
            ViewportContext::setShowWireframe),
        new Toggle("Show vertex UVs", KeyEvent.VK_2,
            ViewportContext::isShowVertexUVs,
            ViewportContext::setShowVertexUVs),
        new Toggle("Show vertex colors", KeyEvent.VK_3,
            ViewportContext::isShowVertexColors,
            ViewportContext::setShowVertexColors),
        new Toggle("Show bounds", KeyEvent.VK_4,
            ViewportContext::isShowBounds,
            ViewportContext::setShowBounds),
        new Toggle("Show skins", KeyEvent.VK_5,
            ViewportContext::isShowSkins,
            ViewportContext::setShowSkins)
    );

    private DebugRenderer debug;
    private Scene scene;
    private SceneStatistics statistics;

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
    public void process(Viewport viewport, ViewportContext context, ViewportInput input, double dt) {
        for (Toggle toggle : toggles) {
            if (input.isKeyPressed(toggle.keyCode)) {
                toggle.toggle(context);
            }
        }
    }

    @Override
    public void draw(Viewport viewport, ViewportContext context, double dt) {
        var scene = viewport.getScene();
        var camera = viewport.getCamera();
        if (scene != null && camera != null) {
            if (this.scene != scene) {
                this.scene = scene;
                this.statistics = SceneStatistics.collect(scene);
            }

            renderScene(scene, camera, context);
            renderInformation(context, statistics, camera);
        }

        if (context.isShowCameraOrigin()) {
            debug.cross(viewport.getCameraOrigin(), 0.1f, false);
        }

        debug.draw(viewport, dt);
    }

    private void renderInformation(ViewportContext context, SceneStatistics statistics, Camera camera) {
        var position = camera.position();
        var text = new StringJoiner("\n");

        text.add("Statistics:");
        text.add("  Vertices %,d".formatted(statistics.vertices));
        text.add("  Faces    %,d".formatted(statistics.faces));
        text.add("  Meshes   %,d".formatted(statistics.meshes));

        text.add("");
        text.add("Position:");
        text.add("  X % f".formatted(position.x()));
        text.add("  Y % f".formatted(position.y()));
        text.add("  Z % f".formatted(position.z()));

        text.add("");
        text.add("Keybinds:");
        for (int i = 0; i < toggles.size(); i++) {
            var toggle = toggles.get(i);
            text.add("  [%d] - %s [%c]".formatted(i + 1, toggle.name(), toggle.get().test(context) ? 'X' : ' '));
        }

        debug.billboardText(text.toString(), 10, 10, 1.0f, 1.0f, 1.0f, 10.0f);
    }

    private void renderScene(Scene scene, Camera camera, ViewportContext context) {
        if (!context.isShowSkins() && !context.isShowBounds()) {
            return;
        }
        scene.accept((node, transform) -> {
            if (context.isShowSkins()) {
                node.skin().ifPresent(skin -> renderSkin(skin, transform.mul(skin.matrix()), camera));
            }
            if (context.isShowBounds()) {
                node.mesh().ifPresent(mesh -> renderBoundingBox(mesh, transform));
            }
            return true;
        });
    }

    private void renderBoundingBox(Mesh mesh, Matrix4f transform) {
        debug.aabb(mesh.computeBoundingBox().transform(transform), Vector3f.one());
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

    private static final class SceneStatistics {
        private long vertices;
        private long faces;
        private int meshes;

        static SceneStatistics collect(Scene scene) {
            var statistics = new SceneStatistics();
            scene.accept((node, _) -> {
                node.mesh().ifPresent(mesh -> {
                    for (Primitive primitive : mesh.primitives()) {
                        statistics.vertices += primitive.positions().count();
                        statistics.faces += primitive.indices().count() / 3;
                    }
                    statistics.meshes += 1;
                });
                return true;
            });
            return statistics;
        }
    }

    private record Toggle(
        String name,
        int keyCode,
        Predicate<ViewportContext> get,
        BiConsumer<ViewportContext, Boolean> set
    ) {
        void toggle(ViewportContext context) {
            set.accept(context, !get.test(context));
        }
    }
}
