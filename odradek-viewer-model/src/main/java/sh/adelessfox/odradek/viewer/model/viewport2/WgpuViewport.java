package sh.adelessfox.odradek.viewer.model.viewport2;

import sh.adelessfox.odradek.math.Vector2f;
import sh.adelessfox.odradek.scene.Scene;
import sh.adelessfox.odradek.ui.Disposable;
import sh.adelessfox.odradek.viewer.model.viewport.Camera;
import sh.adelessfox.odradek.viewer.model.viewport.ViewportInput;
import sh.adelessfox.odradek.viewer.model.viewport2.layers.Layer;
import sh.adelessfox.wgpuj.Queue;
import sh.adelessfox.wgpuj.RenderPass;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public final class WgpuViewport extends WgpuPanel implements Disposable {
    private final ViewportInput input;
    private final Scene scene;
    private final List<Layer> layers;

    private final Camera camera;
    private float cameraSpeed = 5.f;
    private float cameraSensitivity = 0.001f;
    private float cameraDistance;

    private Instant lastUpdateTime = Instant.now();
    private boolean running = true;

    public WgpuViewport(ViewportDescriptor descriptor) {
        this.scene = descriptor.scene();
        this.layers = descriptor.layers();
        this.input = new ViewportInput(this);

        var c = descriptor.camera();
        camera = new Camera(c.fov(), c.near(), c.far());
        camera.position(c.position());
        camera.lookAt(c.target());
        cameraDistance = camera.position().sub(c.target()).length();

        for (Layer layer : layers) {
            layer.onAttach(WgpuViewport.this, device, queue);
        }

        // TODO replace with a proper render loop
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (running) {
                    repaint();
                    SwingUtilities.invokeLater(this);
                }
            }
        });
    }

    @Override
    protected void render(RenderPass pass) {
        var now = Instant.now();
        var delta = Duration.between(lastUpdateTime, now).toMillis() / 1000.0f;

        updateInput(delta);
        renderScene(queue, pass, delta);

        lastUpdateTime = now;
    }

    @Override
    public void dispose() {
        running = false;
        for (Layer layer : layers) {
            layer.onDetach();
        }
        super.dispose();
    }

    public Scene getScene() {
        return scene;
    }

    public Camera getCamera() {
        return camera;
    }

    private void updateInput(float dt) {
        updateCamera(dt);
        input.clear();
    }

    private void renderScene(Queue queue, RenderPass pass, float delta) {
        for (Layer layer : layers) {
            layer.onRender(WgpuViewport.this, queue, pass, delta);
        }
    }

    // region Camera
    private void updateCamera(float dt) {
        if (camera == null) {
            return;
        }

        camera.resize(getWidth(), getHeight());

        var sensitivity = 1.0f;
        var mouseDelta = input.mousePositionDelta().mul(sensitivity);
        var wheelDelta = input.mouseWheelDelta() * sensitivity * 0.1f;

        if (input.isMouseDown(MouseEvent.BUTTON1)) {
            cameraSpeed = Math.clamp((float) Math.exp(Math.log(cameraSpeed) + wheelDelta), 0.1f, 100.0f);
            updateFlyCamera(dt, mouseDelta);
        } else if (input.isMouseDown(MouseEvent.BUTTON2)) {
            updateCameraZoom(Math.clamp((float) Math.exp(Math.log(cameraDistance) - wheelDelta), 0.1f, 100.0f));
            updatePanCamera(dt, mouseDelta);
        } else if (input.isMouseDown(MouseEvent.BUTTON3)) {
            updateCameraZoom(Math.clamp((float) Math.exp(Math.log(cameraDistance) - wheelDelta), 0.1f, 100.0f));
            updateOrbitCamera(mouseDelta);
        }
    }

    private void updateCameraZoom(float newDistance) {
        float delta = newDistance - cameraDistance;
        if (delta != 0.0f) {
            camera.position(camera.position().sub(camera.forward().mul(delta)));
            cameraDistance = newDistance;
        }
    }

    private void updateFlyCamera(float dt, Vector2f mouse) {
        float speed = cameraSpeed * dt;
        if (input.isKeyDown(KeyEvent.VK_SHIFT)) {
            speed *= 5.0f;
        }
        if (input.isKeyDown(KeyEvent.VK_CONTROL)) {
            speed /= 5.0f;
        }

        var position = camera.position();
        var forward = camera.forward().mul(speed);
        var right = camera.right().mul(speed);

        // Horizontal movement
        if (input.isKeyDown(KeyEvent.VK_W)) {
            position = position.add(forward);
        }
        if (input.isKeyDown(KeyEvent.VK_A)) {
            position = position.sub(right);
        }
        if (input.isKeyDown(KeyEvent.VK_S)) {
            position = position.sub(forward);
        }
        if (input.isKeyDown(KeyEvent.VK_D)) {
            position = position.add(right);
        }

        // Vertical movement
        if (input.isKeyDown(KeyEvent.VK_Q)) {
            position = position.sub(0.0f, 0.0f, speed);
        }
        if (input.isKeyDown(KeyEvent.VK_E)) {
            position = position.add(0.0f, 0.0f, speed);
        }

        camera.position(position);
        camera.rotate(mouse.mul(cameraSensitivity));
    }

    private void updatePanCamera(float dt, Vector2f mouse) {
        var speed = (float) (Math.sqrt(cameraDistance) * dt);
        camera.move(camera.right().mul(mouse.x() * speed).negate());
        camera.move(camera.up().mul(mouse.y() * speed));
    }

    private void updateOrbitCamera(Vector2f mouse) {
        var target = camera.forward();
        camera.rotate(mouse.mul(cameraSensitivity));
        var distance = target.sub(camera.forward()).mul(cameraDistance);
        camera.move(distance);
    }
    // endregion
}
