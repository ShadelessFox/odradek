package sh.adelessfox.odradek.hashing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

final class Md5Function extends HashFunction {
    static final Md5Function MD5 = new Md5Function();

    private static final ThreadLocal<MessageDigest> DIGEST = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new ExceptionInInitializerError(e);
        }
    });

    @Override
    public HashCode hash(byte[] input, int off, int len) {
        MessageDigest digest = DIGEST.get();
        digest.update(input, off, len);
        return HashCode.fromBytes(digest.digest());
    }
}
