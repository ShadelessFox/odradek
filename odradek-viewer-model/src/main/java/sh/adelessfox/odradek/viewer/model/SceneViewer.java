package sh.adelessfox.odradek.viewer.model;

import sh.adelessfox.odradek.math.BoundingBox;
import sh.adelessfox.odradek.math.Vector3f;
import sh.adelessfox.odradek.scene.Scene;
import sh.adelessfox.odradek.ui.Viewer;
import sh.adelessfox.odradek.viewer.model.viewport.Camera;
import sh.adelessfox.odradek.viewer.model.viewport.Viewport;
import sh.adelessfox.odradek.viewer.model.viewport.renderpass.GridRenderPass;
import sh.adelessfox.odradek.viewer.model.viewport.renderpass.OverlayRenderPass;
import sh.adelessfox.odradek.viewer.model.viewport.renderpass.RenderMeshesPass;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class SceneViewer implements Viewer<Scene> {
    @Override
    public JComponent createComponent(Scene scene) {
        Camera camera = new Camera(30.f, 0.01f, 1000.f);
        camera.position(scene.computeBoundingBox()
            .map(BoundingBox::center)
            .orElse(Vector3f.zero()));

        Viewport viewport = new Viewport();
        viewport.setMinimumSize(new Dimension(100, 100));
        viewport.addRenderPass(new RenderMeshesPass());
        viewport.addRenderPass(new GridRenderPass());
        viewport.addRenderPass(new OverlayRenderPass());
        viewport.setCamera(camera);
        viewport.setScene(scene);

        return viewport;
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
