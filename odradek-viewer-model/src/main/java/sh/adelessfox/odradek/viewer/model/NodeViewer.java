package sh.adelessfox.odradek.viewer.model;

import sh.adelessfox.odradek.math.Vector3f;
import sh.adelessfox.odradek.scene.Node;
import sh.adelessfox.odradek.scene.Scene;
import sh.adelessfox.odradek.ui.Viewer;
import sh.adelessfox.odradek.viewer.model.viewport.Camera;
import sh.adelessfox.odradek.viewer.model.viewport.Viewport;
import sh.adelessfox.odradek.viewer.model.viewport.renderpass.RenderMeshesPass;

import javax.swing.*;
import java.awt.*;

public class NodeViewer implements Viewer<Node> {
    @Override
    public JComponent createPreview(Node node) {
        Scene scene = Scene.of(node);

        Camera camera = new Camera(30.f, 0.01f, 1000.f);
        camera.position(new Vector3f(-2, 0, 1));

        Viewport viewport = new Viewport();
        viewport.setMinimumSize(new Dimension(100, 100));
        viewport.addRenderPass(new RenderMeshesPass());
        viewport.setCamera(camera);
        viewport.setScene(scene);

        return viewport;
    }

    @Override
    public String displayName() {
        return "Model";
    }
}
