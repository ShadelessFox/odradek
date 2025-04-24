package sh.adelessfox.odradek.hashing;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract sealed class HashFunction
    permits Crc32Function, Fnv1aFunction, Murmur3Function, Md5Function {

    public static HashFunction fnv1a() {
        return Fnv1aFunction.FNV1A;
    }

    public static HashFunction crc32c() {
        return Crc32Function.CRC32C;
    }

    public static HashFunction murmur3() {
        return Murmur3Function.MURMUR3;
    }

    public static HashFunction md5() {
        return Md5Function.MD5;
    }

    public abstract HashCode hash(byte[] input, int off, int len);

    public HashCode hash(byte[] input) {
        return hash(input, 0, input.length);
    }

    public HashCode hash(CharSequence input, Charset charset) {
        return hash(input.toString().getBytes(charset));
    }

    public HashCode hash(CharSequence input) {
        return hash(input, StandardCharsets.UTF_8);
    }
}
