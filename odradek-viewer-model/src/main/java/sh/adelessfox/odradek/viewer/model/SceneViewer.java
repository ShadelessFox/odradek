package sh.adelessfox.odradek.viewer.model;

import com.formdev.flatlaf.extras.components.FlatToolBar;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.scene.Scene;
import sh.adelessfox.odradek.ui.Viewer;
import sh.adelessfox.odradek.ui.components.LineBorder;
import sh.adelessfox.odradek.ui.components.SplitButton;
import sh.adelessfox.odradek.ui.components.properties.PropertyPanelBuilder;
import sh.adelessfox.odradek.ui.util.Fugue;
import sh.adelessfox.odradek.viewer.model.viewport.Camera;
import sh.adelessfox.odradek.viewer.model.viewport.Viewport;
import sh.adelessfox.odradek.viewer.model.viewport.ViewportContext;
import sh.adelessfox.odradek.viewer.model.viewport.renderpass.GridRenderPass;
import sh.adelessfox.odradek.viewer.model.viewport.renderpass.OverlayRenderPass;
import sh.adelessfox.odradek.viewer.model.viewport.renderpass.RenderMeshesPass;
import wtf.reversed.toolbox.math.Bounds;
import wtf.reversed.toolbox.math.Vector3;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.util.Optional;
import java.util.function.Consumer;

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
        Vector3 center = scene.computeBounds()
            .map(Bounds::center)
            .orElse(Vector3.ZERO);

        Camera camera = new Camera(90.f, 0.01f, 1000.f);
        camera.position(center.subtract(new Vector3(1.0f, -1.0f, -1.0f)));

        ViewportContext context = new ViewportContext();
        context.setShowVertexUVs(true);
        context.setShowVertexColors(true);

        Viewport viewport = new Viewport(context);
        viewport.setBorder(LineBorder.of(1, 0, 0, 0));
        viewport.setMinimumSize(new Dimension(100, 100));
        viewport.addRenderPass(new RenderMeshesPass());
        viewport.addRenderPass(new GridRenderPass());
        viewport.addRenderPass(new OverlayRenderPass());
        viewport.setCamera(camera);
        viewport.setCameraOrigin(center);
        viewport.setScene(scene);

        var toolBar = createToolBar(camera, context);

        var panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(toolBar, BorderLayout.NORTH);
        panel.add(viewport, BorderLayout.CENTER);

        return panel;
    }

    private JToolBar createToolBar(Camera camera, ViewportContext context) {
        var toolBar = new FlatToolBar();
        toolBar.add(createPopupButton("Camera", Fugue.getIcon("camera"), menu -> fillCameraPopupMenu(menu, camera, context)));
        toolBar.add(createPopupButton("Scene", Fugue.getIcon("tree"), menu -> fillScenePopupMenu(menu, context)));
        toolBar.setStyle("background: @componentBackground");
        return toolBar;
    }

    private JComponent createPopupButton(String text, Icon icon, Consumer<JPopupMenu> filler) {
        var button = new SplitButton();
        button.setText(text);
        button.setIcon(icon);
        button.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                filler.accept(button.getPopupMenu());
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                button.getPopupMenu().removeAll();
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                button.getPopupMenu().removeAll();
            }
        });
        return button;
    }

    private void fillCameraPopupMenu(JPopupMenu menu, Camera camera, ViewportContext context) {
        // @formatter:off
        var panel = PropertyPanelBuilder.build(pb -> pb
            .category("View", vb -> vb
                .property("Near Clip", camera::nearClip, camera::nearClip, 0.01f, 10.f, 0.01f)
                .property("Far Clip", camera::farClip, camera::farClip, 10.f, 5000.f, 1.0f)
                .property("Field of View", camera::fov, camera::fov, 10.f, 120.f, 1.0f))
            .category("Controls", vb -> vb
                .property("Mouse Sensitivity", context::getCameraMouseSensitivity, context::setCameraMouseSensitivity, 0.1f, 10.0f, 0.1f)
                .property("Fly Speed", context::getCameraSpeed, context::setCameraSpeed, 1.0f, 100.0f, 1.0f)
                .property("<html>Fly Speed (<code>Shift</code> multiplier)", context::getCameraShiftMultiplier, context::setCameraShiftMultiplier, 0.1f, 10.0f, 0.1f)
                .property("<html>Fly Speed (<code>Ctrl</code> multiplier)", context::getCameraCtrlMultiplier, context::setCameraCtrlMultiplier, 0.1f, 10.0f, 0.1f)));
        // @formatter:on

        menu.add(panel);
    }

    private void fillScenePopupMenu(JPopupMenu menu, ViewportContext context) {
        // @formatter:off
        var panel = PropertyPanelBuilder.build(pb -> pb
            .category("Rendering", vb -> vb
                .property("Show wireframe", context::isShowWireframe, context::setShowWireframe)
                .property("Show vertex UVs", context::isShowVertexUVs, context::setShowVertexUVs)
                .property("Show vertex colors", context::isShowVertexColors, context::setShowVertexColors)
                .property("Show bounds", context::isShowBounds, context::setShowBounds)
                .property("Show skeletons", context::isShowSkeletons, context::setShowSkeletons)));

        // @formatter:on

        menu.add(panel);
    }

}
