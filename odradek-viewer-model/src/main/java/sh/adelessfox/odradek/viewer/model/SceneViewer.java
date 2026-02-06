package sh.adelessfox.odradek.viewer.model;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.math.BoundingBox;
import sh.adelessfox.odradek.math.Vector3f;
import sh.adelessfox.odradek.scene.Scene;
import sh.adelessfox.odradek.ui.Viewer;
import sh.adelessfox.odradek.viewer.model.viewport2.CameraDescriptor;
import sh.adelessfox.odradek.viewer.model.viewport2.Viewport2;
import sh.adelessfox.odradek.viewer.model.viewport2.ViewportDescriptor;
import sh.adelessfox.odradek.viewer.model.viewport2.layers.DemoLayer;

import javax.swing.*;
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
    private Viewport2 viewport;

    private SceneViewer(Scene scene) {
        this.scene = scene;
    }

    @Override
    public JComponent createComponent() {
        var center = scene.computeBoundingBox()
            .map(BoundingBox::center)
            .orElse(Vector3f.zero());

        var descriptor = ViewportDescriptor.builder()
            .camera(CameraDescriptor.builder()
                .fov(30.0f)
                .near(0.01f).far(1000.0f)
                .position(center.sub(1.0f, -1.0f, -1.0f))
                .target(center)
                .build())
            .scene(scene)
            .addLayers(new DemoLayer())
            .build();

        return viewport = new Viewport2(descriptor);
    }

    @Override
    public void dispose() {
        viewport.dispose();
    }
}
