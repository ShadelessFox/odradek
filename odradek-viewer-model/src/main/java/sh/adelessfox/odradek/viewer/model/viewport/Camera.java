package sh.adelessfox.odradek.viewer.model.viewport;

import sh.adelessfox.odradek.math.Mat4f;
import sh.adelessfox.odradek.math.Vec3f;

public final class Camera {
    private static final float PITCH_LIMIT = (float) Math.PI / 2 - 0.01f;

    private int width, height;
    private float x, y, z;
    private float fov;
    private float near, far;
    private float yaw, pitch;

    public Camera(float fov, float near, float far) {
        this.fov = fov;
        this.near = near;
        this.far = far;
    }

    public void rotate(float deltaX, float deltaY) {
        yaw -= (float) (Math.PI * deltaX / width);
        pitch -= (float) (Math.PI * deltaY / height);
        pitch = Math.clamp(pitch, -PITCH_LIMIT, PITCH_LIMIT);
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void lookAt(Vec3f target) {
        Vec3f dir = target.sub(x, y, z).normalize();
        yaw = (float) Math.atan2(dir.y(), dir.x());
        pitch = (float) Math.asin(dir.z());
        pitch = Math.clamp(pitch, -PITCH_LIMIT, PITCH_LIMIT);
    }

    public Mat4f projectionView() {
        return projection().mul(view());
    }

    public Mat4f projection() {
        var aspect = (float) width / height;
        return Mat4f.perspective(fov, aspect, near, far);
    }

    public Mat4f view() {
        var eye = position();
        var center = forward().add(eye);
        return Mat4f.lookAt(eye, center, up());
    }

    public Vec3f position() {
        return new Vec3f(x, y, z);
    }

    public void position(Vec3f position) {
        this.x = position.x();
        this.y = position.y();
        this.z = position.z();
    }

    public Vec3f up() {
        return new Vec3f(0.f, 0.f, -1.f);
    }

    public Vec3f forward() {
        float yawSin = (float) Math.sin(yaw);
        float yawCos = (float) Math.cos(yaw);
        float pitchSin = (float) Math.sin(pitch);
        float pitchCos = (float) Math.cos(pitch);
        return new Vec3f(yawCos * pitchCos, yawSin * pitchCos, pitchSin);
    }

    public Vec3f right() {
        float x = (float) Math.cos(yaw - Math.PI * 0.5);
        float y = (float) Math.sin(yaw - Math.PI * 0.5);
        return new Vec3f(x, y, 0);
    }

    public float near() {
        return near;
    }

    public float far() {
        return far;
    }
}
