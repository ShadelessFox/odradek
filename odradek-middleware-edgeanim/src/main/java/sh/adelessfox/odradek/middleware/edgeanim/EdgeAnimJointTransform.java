package sh.adelessfox.odradek.middleware.edgeanim;

import sh.adelessfox.odradek.io.BinaryReader;
import wtf.reversed.toolbox.math.Matrix4;
import wtf.reversed.toolbox.math.Quaternion;

import java.io.IOException;
import java.util.Arrays;

public record EdgeAnimJointTransform(
    float[] rotation,
    float[] translation,
    float[] scale
) {
    public static EdgeAnimJointTransform read(BinaryReader reader) throws IOException {
        return new EdgeAnimJointTransform(
            reader.readFloats(4),
            reader.readFloats(4),
            reader.readFloats(4)
        );
    }

    public Matrix4 toMatrix() {
        return Matrix4.IDENTITY
            .multiply(Matrix4.fromTranslation(translation[0], translation[1], translation[2]))
            .multiply(Matrix4.fromRotation(new Quaternion(rotation[0], rotation[1], rotation[2], rotation[3])))
            .multiply(Matrix4.fromScale(scale[0], scale[1], scale[2]));
    }

    @Override
    public String toString() {
        return "EdgeAnimJointTransform{" +
            "rotation=" + Arrays.toString(rotation) +
            ", translation=" + Arrays.toString(translation) +
            ", scale=" + Arrays.toString(scale) +
            '}';
    }
}
