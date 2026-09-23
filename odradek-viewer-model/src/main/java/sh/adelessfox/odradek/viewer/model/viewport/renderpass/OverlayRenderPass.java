package sh.adelessfox.odradek.viewer.model.viewport.renderpass;

import com.formdev.flatlaf.util.UIScale;
import sh.adelessfox.odradek.geometry.Mesh;
import sh.adelessfox.odradek.scene.Bone;
import sh.adelessfox.odradek.scene.Node;
import sh.adelessfox.odradek.scene.Scene;
import sh.adelessfox.odradek.scene.Skeleton;
import sh.adelessfox.odradek.viewer.model.viewport.Camera;
import sh.adelessfox.odradek.viewer.model.viewport.Viewport;
import sh.adelessfox.odradek.viewer.model.viewport.ViewportContext;
import wtf.reversed.toolbox.math.Bounds;
import wtf.reversed.toolbox.math.Matrix4;
import wtf.reversed.toolbox.math.Vector3;

import java.io.IOException;
import java.util.*;

public class OverlayRenderPass implements RenderPass {
    private static final int MAX_JOINTS_TO_DISPLAY_NAMES_FOR = 128;

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
            renderInformation(statistics, camera);
        }

        if (context.isShowCameraOrigin()) {
            debug.cross(viewport.getCameraOrigin(), 0.1f, false);
        }

        debug.draw(viewport, dt);
    }

    private void renderInformation(SceneStatistics statistics, Camera camera) {
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

        debug.billboardText(text.toString(), 10, 10, 1.0f, 1.0f, 1.0f, UIScale.scale(10.0f));
    }

    private void renderNodes(Camera camera, ViewportContext context) {
        if (!context.isShowSkeletons() && !context.isShowBounds()) {
            return;
        }
        for (OverlayNode node : nodes) {
            if (context.isShowSkeletons()) {
                node.skeleton().ifPresent(skeleton -> renderSkeleton(skeleton, node.transform(), camera));
            }
            if (context.isShowBounds()) {
                for (OverlayMesh mesh : node.meshes()) {
                    debug.aabb(mesh.worldBounds(), mesh.color());
                }
                node.worldBounds().ifPresent(bounds -> debug.aabb(bounds, new Vector3(1, 1, 1)));
            }
        }
    }

    private void renderSkeleton(Skeleton skeleton, Matrix4 transform, Camera camera) {
        var matrices = new ArrayList<Matrix4>(skeleton.bones().size());

        for (Bone bone : skeleton.bones()) {
            Matrix4 boneMatrix;
            Matrix4 parentMatrix;

            if (bone.parent().isPresent()) {
                parentMatrix = matrices.get(bone.parent().getAsInt());
                boneMatrix = parentMatrix.multiply(bone.matrix());
            } else {
                parentMatrix = null;
                boneMatrix = transform.multiply(bone.matrix());
            }

            var position = boneMatrix.toTranslation();

            if (parentMatrix != null) {
                debug.line(parentMatrix.toTranslation(), position, new Vector3(0, 1, 0), false);
            }

            var distance = position.distance(camera.position());
            debug.point(position, new Vector3(1, 0, 1), 2.0f / distance, false);

            if (skeleton.bones().size() <= MAX_JOINTS_TO_DISPLAY_NAMES_FOR) {
                debug.projectedText(bone.name(), position, camera, new Vector3(1, 1, 1), 4.0f / distance);
            }

            matrices.add(boneMatrix);
        }
    }

    private void cacheSceneNodes(Scene scene) {
        nodes.clear();
        scene.accept((node, worldTransform) -> {
            computeNode(node, worldTransform).ifPresent(nodes::add);
            return true;
        });
    }

    private Optional<OverlayNode> computeNode(Node node, Matrix4 worldTransform) {
        var worldBounds = node.model().flatMap(model -> model.computeBounds(worldTransform));
        var meshes = node.model().stream()
            .flatMap(model -> model.meshes().stream())
            .map(mesh -> cacheNode(mesh, worldTransform))
            .flatMap(Optional::stream)
            .toList();
        if (meshes.isEmpty() && node.skeleton().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new OverlayNode(node.skeleton(), meshes, worldTransform, worldBounds));
    }

    private static Optional<OverlayMesh> cacheNode(Mesh mesh, Matrix4 worldTransform) {
        var bounds = mesh.computeBounds().orElse(null);
        if (bounds == null) {
            return Optional.empty();
        }
        return Optional.of(new OverlayMesh(
            bounds.transform(worldTransform),
            computeRandomColor(mesh.hashCode())));
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

    private record OverlayNode(
        Optional<Skeleton> skeleton,
        List<OverlayMesh> meshes,
        Matrix4 transform,
        Optional<Bounds> worldBounds
    ) {
    }

    private record OverlayMesh(Bounds worldBounds, Vector3 color) {
    }
}
