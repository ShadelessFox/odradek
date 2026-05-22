package sh.adelessfox.odradek.viewer.model.viewport;

import wtf.reversed.toolbox.math.FloatMath;
import wtf.reversed.toolbox.math.Matrix4;
import wtf.reversed.toolbox.math.Vector2;
import wtf.reversed.toolbox.math.Vector3;

public final class Camera {
    private static final float PITCH_LIMIT = FloatMath.PI_2 - 0.01f;

    private Vector3 position;
    private Vector2 viewport;
    private float fov;
    private float nearClip, farClip;
    private float yaw, pitch;

    public Camera(float fov, float nearClip, float farClip) {
        this.fov = fov;
        this.nearClip = nearClip;
        this.farClip = farClip;
    }

    public void rotate(float deltaX, float deltaY) {
        yaw -= (float) (Math.PI * deltaX / viewport.x());
        pitch -= (float) (Math.PI * deltaY / viewport.y());
        pitch = Math.clamp(pitch, -PITCH_LIMIT, PITCH_LIMIT);
    }

    public void resize(float width, float height) {
        this.viewport = new Vector2(width, height);
    }

    public void lookAt(Vector3 target) {
        Vector3 dir = target.subtract(position).normalize();
        yaw = (float) Math.atan2(dir.y(), dir.x());
        pitch = (float) Math.asin(dir.z());
        pitch = Math.clamp(pitch, -PITCH_LIMIT, PITCH_LIMIT);
    }

    public Matrix4 projectionView() {
        return projection().multiply(view());
    }

    public Matrix4 projection() {
        return Matrix4.perspective((float) Math.toRadians(fov), viewport.x() / viewport.y(), nearClip, farClip);
    }

    public Matrix4 view() {
        var eye = position();
        var center = forward().add(eye);
        return Matrix4.lookAt(eye, center, Vector3.Z);
    }

    public Vector3 position() {
        return position;
    }

    public void position(Vector3 position) {
        this.position = position;
    }

    public void move(Vector3 delta) {
        this.position = position().add(delta);
    }

    public Vector3 up() {
        return right().cross(forward());
    }

    public Vector3 forward() {
        float yawSin = (float) Math.sin(yaw);
        float yawCos = (float) Math.cos(yaw);
        float pitchSin = (float) Math.sin(pitch);
        float pitchCos = (float) Math.cos(pitch);
        return new Vector3(yawCos * pitchCos, yawSin * pitchCos, pitchSin);
    }

    public Vector3 right() {
        float x = (float) Math.cos(yaw - Math.PI * 0.5);
        float y = (float) Math.sin(yaw - Math.PI * 0.5);
        return new Vector3(x, y, 0);
    }

    public float fov() {
        return fov;
    }

    public void fov(float fov) {
        this.fov = fov;
    }

    public float nearClip() {
        return nearClip;
    }

    public void nearClip(float near) {
        this.nearClip = near;
    }

    public float farClip() {
        return farClip;
    }

    public void farClip(float farClip) {
        this.farClip = farClip;
    }
}
