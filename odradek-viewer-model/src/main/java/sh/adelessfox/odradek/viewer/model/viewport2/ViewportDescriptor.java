package sh.adelessfox.odradek.viewer.model.viewport2;

import org.immutables.value.Value;
import sh.adelessfox.odradek.scene.Scene;
import sh.adelessfox.odradek.viewer.model.viewport2.layers.Layer;

import java.util.List;

@Value.Builder
public record ViewportDescriptor(
    CameraDescriptor camera,
    Scene scene,
    List<Layer> layers
) {
    public ViewportDescriptor {
        layers = List.copyOf(layers);
    }

    public static ViewportDescriptorBuilder builder() {
        return new ViewportDescriptorBuilder();
    }
}
