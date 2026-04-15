package sh.adelessfox.odradek.util;

// TODO: Introduce a type for abstract file paths
public final class Filenames {
    private Filenames() {
    }

    public static String filename(String path) {
        int sep = path.lastIndexOf('/');
        if (sep >= 0) {
            path = path.substring(sep + 1);
        }
        return path;
    }

    public static String withSuffix(String filename, String suffix) {
        int ext = filename.lastIndexOf('.');
        if (ext >= 0) {
            filename = filename.substring(0, ext);
        }
        return filename + suffix;
    }
}
