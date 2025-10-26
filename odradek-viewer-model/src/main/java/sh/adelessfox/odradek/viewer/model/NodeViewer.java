package sh.adelessfox.odradek.viewer.model;

import sh.adelessfox.odradek.math.Vector3f;
import sh.adelessfox.odradek.scene.Node;
import sh.adelessfox.odradek.scene.Scene;
import sh.adelessfox.odradek.ui.Viewer;
import sh.adelessfox.odradek.viewer.model.viewport.Camera;
import sh.adelessfox.odradek.viewer.model.viewport.Viewport;
import sh.adelessfox.odradek.viewer.model.viewport.renderpass.GridRenderPass;
import sh.adelessfox.odradek.viewer.model.viewport.renderpass.RenderMeshesPass;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class NodeViewer implements Viewer<Node> {
    @Override
    public JComponent createComponent(Node node) {
        Scene scene = Scene.of(node);

        Camera camera = new Camera(30.f, 0.01f, 1000.f);
        camera.position(new Vector3f(-2, 0, 1));

        Viewport viewport = new Viewport();
        viewport.setMinimumSize(new Dimension(100, 100));
        viewport.addRenderPass(new RenderMeshesPass());
        viewport.addRenderPass(new GridRenderPass());
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
