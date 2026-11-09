package sh.adelessfox.odradek.middleware.edgeanim;

import wtf.reversed.toolbox.collect.Floats;
import wtf.reversed.toolbox.io.BinarySource;
import wtf.reversed.toolbox.math.Matrix4;
import wtf.reversed.toolbox.math.Quaternion;

import java.io.IOException;

public record EdgeAnimJointTransform(
    Floats rotation,
    Floats translation,
    Floats scale
) {
    public static EdgeAnimJointTransform read(BinarySource source) throws IOException {
        return new EdgeAnimJointTransform(
            source.readFloats(4),
            source.readFloats(4),
            source.readFloats(4)
        );
    }

    public Matrix4 toMatrix() {
        return Matrix4.IDENTITY
            .multiply(Matrix4.fromTranslation(translation.get(0), translation.get(1), translation.get(2)))
            .multiply(Matrix4.fromRotation(new Quaternion(rotation.get(0), rotation.get(1), rotation.get(2), rotation.get(3))))
            .multiply(Matrix4.fromScale(scale.get(0), scale.get(1), scale.get(2)));
    }
}
