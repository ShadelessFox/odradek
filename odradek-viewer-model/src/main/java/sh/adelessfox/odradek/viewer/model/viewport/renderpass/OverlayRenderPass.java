package sh.adelessfox.odradek.viewer.model.viewport.renderpass;

import sh.adelessfox.odradek.geometry.Mesh;
import sh.adelessfox.odradek.geometry.Model;
import sh.adelessfox.odradek.scene.Joint;
import sh.adelessfox.odradek.scene.Node;
import sh.adelessfox.odradek.scene.Scene;
import sh.adelessfox.odradek.scene.Skin;
import sh.adelessfox.odradek.viewer.model.viewport.Camera;
import sh.adelessfox.odradek.viewer.model.viewport.Viewport;
import sh.adelessfox.odradek.viewer.model.viewport.ViewportContext;
import sh.adelessfox.odradek.viewer.model.viewport.ViewportInput;
import wtf.reversed.toolbox.math.Bounds;
import wtf.reversed.toolbox.math.Matrix4;
import wtf.reversed.toolbox.math.Vector3;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class OverlayRenderPass implements RenderPass {
    private static final int MAX_JOINTS_TO_DISPLAY_NAMES_FOR = 128;

    private static final List<Toggle> toggles = List.of(
        new Toggle("Show wireframe", KeyEvent.VK_1, ViewportContext::isShowWireframe, ViewportContext::setShowWireframe),
        new Toggle("Show vertex UVs", KeyEvent.VK_2, ViewportContext::isShowVertexUVs, ViewportContext::setShowVertexUVs),
        new Toggle("Show vertex colors", KeyEvent.VK_3, ViewportContext::isShowVertexColors, ViewportContext::setShowVertexColors),
        new Toggle("Show bounds", KeyEvent.VK_4, ViewportContext::isShowBounds, ViewportContext::setShowBounds),
        new Toggle("Show skins", KeyEvent.VK_5, ViewportContext::isShowSkins, ViewportContext::setShowSkins)
    );

    private DebugRenderer debug;
    private Scene scene;
    private SceneStatistics statistics;
    private final List<OverlayNode> nodes = new ArrayList<>();

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
                cacheSceneNodes(scene);
            }

            renderNodes(camera, context);
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

    private void renderNodes(Camera camera, ViewportContext context) {
        if (!context.isShowSkins() && !context.isShowBounds()) {
            return;
        }
        for (OverlayNode node : nodes) {
            if (context.isShowSkins()) {
                node.skin().ifPresent(skin -> renderSkin(skin, node.transform(), camera));
            }
            if (context.isShowBounds()) {
                for (OverlayMesh mesh : node.meshes()) {
                    debug.aabb(mesh.bounds().transform(node.transform()), mesh.color());
                }
            }
        }
    }

    private void renderSkin(Skin skin, Matrix4 transform, Camera camera) {
        var matrices = new ArrayList<Matrix4>(skin.joints().size());

        for (Joint joint : skin.joints()) {
            Matrix4 jointMatrix;
            Matrix4 parentMatrix;

            if (joint.parent().isPresent()) {
                parentMatrix = matrices.get(joint.parent().getAsInt());
                jointMatrix = parentMatrix.multiply(joint.matrix());
            } else {
                parentMatrix = null;
                jointMatrix = transform.multiply(joint.matrix());
            }

            var position = jointMatrix.toTranslation();

            if (parentMatrix != null) {
                debug.line(parentMatrix.toTranslation(), position, new Vector3(0, 1, 0), false);
            }

            var distance = position.distance(camera.position());
            debug.point(position, new Vector3(1, 0, 1), 2.0f / distance, false);

            if (skin.joints().size() <= MAX_JOINTS_TO_DISPLAY_NAMES_FOR) {
                debug.projectedText(joint.name(), position, camera, new Vector3(1, 1, 1), 4.0f / distance);
            }

            matrices.add(jointMatrix);
        }
    }

    private void cacheSceneNodes(Scene scene) {
        nodes.clear();
        for (Node node : scene.nodes()) {
            cacheNodeRecursively(node, node.matrix());
        }
    }

    private void cacheNodeRecursively(Node node, Matrix4 transform) {
        var meshes = node.model().map(Model::meshes).orElse(List.of());
        var overlayMeshes = meshes.stream()
            .map(mesh -> new OverlayMesh(mesh, mesh.computeBounds(), computeRandomColor(mesh.hashCode())))
            .toList();
        nodes.add(new OverlayNode(node.skin(), overlayMeshes, transform));
        for (Node child : node.children()) {
            cacheNodeRecursively(child, transform.multiply(child.matrix()));
        }
    }

    private static Vector3 computeRandomColor(int seed) {
        var random = new Random(seed);
        return new Vector3(
            random.nextFloat(0.5f, 1.0f),
            random.nextFloat(0.5f, 1.0f),
            random.nextFloat(0.5f, 1.0f)
        );
    }

    private static final class SceneStatistics {
        private long vertices;
        private long faces;
        private int meshes;

        static SceneStatistics collect(Scene scene) {
            var statistics = new SceneStatistics();
            scene.accept((node, _) -> {
                node.model().ifPresent(model -> {
                    for (Mesh mesh : model.meshes()) {
                        statistics.vertices += mesh.positions().length() / 3;
                        statistics.faces += mesh.indices().length() / 3;
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

    private record OverlayNode(Optional<Skin> skin, List<OverlayMesh> meshes, Matrix4 transform) {
    }

    private record OverlayMesh(Mesh mesh, Bounds bounds, Vector3 color) {
    }
}
