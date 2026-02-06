package sh.adelessfox.odradek.viewer.model.viewport2;

import org.immutables.value.Value;
import sh.adelessfox.odradek.math.Vector3f;

@Value.Builder
public record CameraDescriptor(
    float fov,
    float near,
    float far,
    Vector3f position,
    Vector3f target
) {
    public static CameraDescriptorBuilder builder() {
        return new CameraDescriptorBuilder();
    }
}
