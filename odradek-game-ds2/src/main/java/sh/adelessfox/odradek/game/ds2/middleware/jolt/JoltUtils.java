package sh.adelessfox.odradek.game.ds2.middleware.jolt;

import sh.adelessfox.odradek.game.ds2.middleware.jolt.geometry.AABox;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.math.Mat44;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.math.Quat;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.math.Vec3;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.math.Vec4;
import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;
import java.util.List;

public final class JoltUtils {
    private JoltUtils() {
        // prevents instantiation
    }

    public static Vec3 readVec3(BinarySource source) throws IOException {
        var x = source.readFloat();
        var y = source.readFloat();
        var z = source.readFloat();

        return new Vec3(x, y, z);
    }

    public static Vec3 readAlignedVector3(BinarySource source) throws IOException {
        var x = source.readFloat();
        var y = source.readFloat();
        var z = source.readFloat();
        var w = source.readFloat();

        if (z != w) {
            throw new IllegalArgumentException("z and w must be equal");
        }

        return new Vec3(x, y, z);
    }

    public static Vec4 readVec4(BinarySource source) throws IOException {
        var x = source.readFloat();
        var y = source.readFloat();
        var z = source.readFloat();
        var w = source.readFloat();

        return new Vec4(x, y, z, w);
    }

    public static Quat readQuaternion(BinarySource source) throws IOException {
        var x = source.readFloat();
        var y = source.readFloat();
        var z = source.readFloat();
        var w = source.readFloat();

        return new Quat(x, y, z, w);
    }

    public static Mat44 readMatrix4(BinarySource source) throws IOException {
        var col0 = readVec4(source);
        var col1 = readVec4(source);
        var col2 = readVec4(source);
        var col3 = readVec4(source);

        return new Mat44(col0, col1, col2, col3);
    }

    public static AABox readAABox(BinarySource source) throws IOException {
        return new AABox(readVec3(source), readVec3(source));
    }

    public static <T> List<T> readObjects(BinarySource source, BinarySource.Mapper<T> mapper) throws IOException {
        return source.readObjects(readCount(source), mapper);
    }

    public static Bytes readBytes(BinarySource source) throws IOException {
        return source.readBytes(readCount(source));
    }

    public static String readString(BinarySource source) throws IOException {
        return source.readString(readCount(source));
    }

    private static int readCount(BinarySource source) throws IOException {
        return Math.toIntExact(source.readLong());
    }
}
