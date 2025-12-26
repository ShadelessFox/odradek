package sh.adelessfox.odradek.viewer.shader.win32;

public record CLSID(GUID guid) {
    public static CLSID of(String name) {
        return new CLSID(GUID.of(name));
    }
}
