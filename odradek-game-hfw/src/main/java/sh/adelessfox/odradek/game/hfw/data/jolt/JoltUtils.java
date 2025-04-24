package sh.adelessfox.odradek.game.hfw.data.jolt;

import sh.adelessfox.odradek.ThrowableFunction;
import sh.adelessfox.odradek.game.hfw.data.jolt.geometry.AABox;
import sh.adelessfox.odradek.game.hfw.data.jolt.math.Mat44;
import sh.adelessfox.odradek.game.hfw.data.jolt.math.Quat;
import sh.adelessfox.odradek.game.hfw.data.jolt.math.Vec3;
import sh.adelessfox.odradek.game.hfw.data.jolt.math.Vec4;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;
import java.util.List;

public final class JoltUtils {
    private JoltUtils() {
        // prevents instantiation
    }

    public static Vec3 readVec3(BinaryReader reader) throws IOException {
        var x = reader.readFloat();
        var y = reader.readFloat();
        var z = reader.readFloat();

        return new Vec3(x, y, z);
    }

    public static Vec3 readAlignedVector3(BinaryReader reader) throws IOException {
        var x = reader.readFloat();
        var y = reader.readFloat();
        var z = reader.readFloat();
        var w = reader.readFloat();

        if (z != w) {
            throw new IllegalArgumentException("z and w must be equal");
        }

        return new Vec3(x, y, z);
    }

    public static Vec4 readVec4(BinaryReader reader) throws IOException {
        var x = reader.readFloat();
        var y = reader.readFloat();
        var z = reader.readFloat();
        var w = reader.readFloat();

        return new Vec4(x, y, z, w);
    }

    public static Quat readQuaternion(BinaryReader reader) throws IOException {
        var x = reader.readFloat();
        var y = reader.readFloat();
        var z = reader.readFloat();
        var w = reader.readFloat();

        return new Quat(x, y, z, w);
    }

    public static Mat44 readMatrix4(BinaryReader reader) throws IOException {
        var col0 = readVec4(reader);
        var col1 = readVec4(reader);
        var col2 = readVec4(reader);
        var col3 = readVec4(reader);

        return new Mat44(col0, col1, col2, col3);
    }

    public static AABox readAABox(BinaryReader reader) throws IOException {
        return new AABox(readAlignedVector3(reader), readAlignedVector3(reader));
    }

    public static <T> List<T> readObjects(BinaryReader reader, ThrowableFunction<BinaryReader, T, IOException> mapper) throws IOException {
        return reader.readObjects(readCount(reader), mapper);
    }

    public static byte[] readBytes(BinaryReader reader) throws IOException {
        return reader.readBytes(readCount(reader));
    }

    public static String readString(BinaryReader reader) throws IOException {
        return reader.readString(readCount(reader));
    }

    private static int readCount(BinaryReader reader) throws IOException {
        return Math.toIntExact(reader.readLong());
    }
}
