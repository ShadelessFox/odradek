package sh.adelessfox.odradek.viewer.shader.win32;

public final class Win32Exception extends RuntimeException {
    private final int result;

    public Win32Exception(int result) {
        this.result = result;
    }

    public static void check(int rc) {
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
    }

    @Override
    public String getMessage() {
        return "0x%08x".formatted(result);
    }
}
