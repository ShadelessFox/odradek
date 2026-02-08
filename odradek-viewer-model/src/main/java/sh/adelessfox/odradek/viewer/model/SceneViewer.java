package sh.adelessfox.odradek.viewer.model;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.math.BoundingBox;
import sh.adelessfox.odradek.math.Vector3f;
import sh.adelessfox.odradek.scene.Scene;
import sh.adelessfox.odradek.ui.Viewer;
import sh.adelessfox.odradek.viewer.model.viewport.Camera;
import sh.adelessfox.odradek.viewer.model.viewport.Viewport;
import sh.adelessfox.odradek.viewer.model.viewport.renderpass.GridRenderPass;
import sh.adelessfox.odradek.viewer.model.viewport.renderpass.OverlayRenderPass;
import sh.adelessfox.odradek.viewer.model.viewport.renderpass.RenderMeshesPass;
import sh.adelessfox.odradek.viewer.model.viewport2.CameraDescriptor;
import sh.adelessfox.odradek.viewer.model.viewport2.ViewportDescriptor;
import sh.adelessfox.odradek.viewer.model.viewport2.WgpuViewport;
import sh.adelessfox.odradek.viewer.model.viewport2.layers.GridLayer;
import sh.adelessfox.odradek.viewer.model.viewport2.layers.MeshLayer;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public final class SceneViewer implements Viewer {
    public static final class Provider implements Viewer.Provider<Scene> {
        @Override
        public Viewer create(Scene object, Game game) {
            return new SceneViewer(object);
        }

        @Override
        public String name() {
            return "Model";
        }

        @Override
        public Optional<String> icon() {
            return Optional.of("fugue:block");
        }
    }

    private final Scene scene;
    private WgpuViewport viewport;

    private SceneViewer(Scene scene) {
        this.scene = scene;
    }

    @Override
    public JComponent createComponent() {
        var center = scene.computeBoundingBox()
            .map(BoundingBox::center)
            .orElse(Vector3f.zero());

        if (false) {
            Camera camera = new Camera(30.f, 0.01f, 1000.f);
            camera.position(center.sub(1.0f, -1.0f, -1.0f));

            Viewport viewport = new Viewport();
            viewport.setMinimumSize(new Dimension(100, 100));
            viewport.addRenderPass(new RenderMeshesPass());
            viewport.addRenderPass(new GridRenderPass());
            viewport.addRenderPass(new OverlayRenderPass());
            viewport.setCamera(camera);
            viewport.setCameraOrigin(center);
            viewport.setScene(scene);

            return viewport;
        }

        var descriptor = ViewportDescriptor.builder()
            .camera(CameraDescriptor.builder()
                .fov(30.0f)
                .near(0.01f).far(1000.0f)
                .position(center.sub(1.0f, -1.0f, -1.0f))
                .target(center)
                .build())
            .scene(scene)
            .addLayers(new GridLayer())
            .addLayers(new MeshLayer())
            .build();

        return viewport = new WgpuViewport(descriptor);
    }

    @Override
    public void dispose() {
        viewport.dispose();
    }
}
