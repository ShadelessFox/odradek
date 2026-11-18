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
import java.time.Duration;
import java.util.*;

public class OverlayRenderPass implements RenderPass {
    private DebugRenderer debug;
    private Scene scene;
    private List<OverlayNode> nodes;
    private SceneStatistics statistics;

    // NOTE: Move to ViewportAnimator at some point, or introduce a helper class
    private int frameCount;
    private int lastFrameCount;
    private long lastFrameCommitTime = System.currentTimeMillis();

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
                this.nodes = collectSceneNodes(scene);
                this.statistics = SceneStatistics.collect(scene);
            }

            renderNodes(camera, context);
            renderInformation(
                statistics,
                camera,
                lastFrameCount,
                viewport.getLastFrameDuration(),
                viewport.getLastFrameSleep());
        }

        if (context.isShowCameraOrigin()) {
            debug.cross(viewport.getCameraOrigin(), 0.1f, false);
        }

        debug.draw(viewport, dt);

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameCommitTime >= 1000) {
            lastFrameCount = frameCount;
            frameCount = 0;
            lastFrameCommitTime = currentTime;
        }

        frameCount++;
    }

    private void renderInformation(
        SceneStatistics statistics,
        Camera camera,
        int fps,
        Duration lastFrameDuration,
        Duration lastFrameSleep
    ) {
        var position = camera.position();
        var text = new StringJoiner("\n");

        text.add("FPS: %3d (%.2f ms render; %.2f ms sleep)".formatted(
            fps,
            lastFrameDuration.toNanos() / 1_000_000.0,
            lastFrameSleep.toNanos() / 1_000_000.0));

        text.add("");
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
                node.skeleton().ifPresent(skeleton -> renderSkeleton(skeleton, node.transform(), camera, context));
            }
            if (context.isShowBounds()) {
                for (OverlayMesh mesh : node.meshes()) {
                    debug.aabb(mesh.worldBounds(), mesh.color());
                }
                node.worldBounds().ifPresent(bounds -> debug.aabb(bounds, new Vector3(1, 1, 1)));
            }
        }
    }

    private void renderSkeleton(Skeleton skeleton, Matrix4 transform, Camera camera, ViewportContext context) {
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

            if (context.isShowBoneNames()) {
                debug.projectedText(bone.name(), position, camera, new Vector3(1, 1, 1), 4.0f / distance);
            }

            matrices.add(boneMatrix);
        }
    }

    private static List<OverlayNode> collectSceneNodes(Scene scene) {
        var nodes = new ArrayList<OverlayNode>();
        var skeletons = new HashSet<SkeletonInstance>();
        scene.accept((node, worldTransform) -> {
            var skeleton = node.skeleton().filter(s -> skeletons.add(new SkeletonInstance(s, worldTransform)));
            computeNode(node, worldTransform, skeleton).ifPresent(nodes::add);
            return true;
        });
        return List.copyOf(nodes);
    }

    private static Optional<OverlayNode> computeNode(Node node, Matrix4 worldTransform, Optional<Skeleton> skeleton) {
        var worldBounds = node.model().flatMap(model -> model.computeBounds(worldTransform));
        var meshes = node.model().stream()
            .flatMap(model -> model.meshes().stream())
            .map(mesh -> cacheNode(mesh, worldTransform))
            .flatMap(Optional::stream)
            .toList();
        if (meshes.isEmpty() && skeleton.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new OverlayNode(skeleton, meshes, worldTransform, worldBounds));
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

    private record SkeletonInstance(Skeleton skeleton, Matrix4 transform) {
    }
}
