package sh.adelessfox.odradek.game.hfw.data.edge;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.math.Matrix4f;
import sh.adelessfox.odradek.math.Quaternionf;

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

    public Matrix4f toMatrix() {
        return Matrix4f.identity()
            .translate(translation[0], translation[1], translation[2])
            .rotate(new Quaternionf(rotation[0], rotation[1], rotation[2], rotation[3]))
            .scale(scale[0], scale[1], scale[2]);
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
