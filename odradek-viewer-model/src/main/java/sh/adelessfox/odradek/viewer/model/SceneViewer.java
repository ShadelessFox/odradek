package sh.adelessfox.odradek.viewer.model;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.scene.Scene;
import sh.adelessfox.odradek.ui.Viewer;
import sh.adelessfox.odradek.viewer.model.viewport.Camera;
import sh.adelessfox.odradek.viewer.model.viewport.Viewport;
import sh.adelessfox.odradek.viewer.model.viewport.ViewportContext;
import sh.adelessfox.odradek.viewer.model.viewport.renderpass.GridRenderPass;
import sh.adelessfox.odradek.viewer.model.viewport.renderpass.OverlayRenderPass;
import sh.adelessfox.odradek.viewer.model.viewport.renderpass.RenderMeshesPass;
import wtf.reversed.toolbox.math.Bounds;
import wtf.reversed.toolbox.math.Vector3;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public record SceneViewer(Scene scene) implements Viewer {
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

    @Override
    public JComponent createComponent() {
        Vector3 center = scene.computeBoundingBox()
            .map(Bounds::center)
            .orElse(Vector3.ZERO);

        Camera camera = new Camera(30.f, 0.01f, 1000.f);
        camera.position(center.subtract(new Vector3(1.0f, -1.0f, -1.0f)));

        ViewportContext context = new ViewportContext();
        context.setShowVertexUVs(true);
        context.setShowVertexColors(true);

        Viewport viewport = new Viewport(context);
        viewport.setMinimumSize(new Dimension(100, 100));
        viewport.addRenderPass(new RenderMeshesPass());
        viewport.addRenderPass(new GridRenderPass());
        viewport.addRenderPass(new OverlayRenderPass());
        viewport.setCamera(camera);
        viewport.setCameraOrigin(center);
        viewport.setScene(scene);

        return viewport;
    }
}
