package sh.adelessfox.odradek.util;

import java.util.Locale;

public enum OperatingSystem {
    WINDOWS,
    LINUX,
    MACOS;

    private static final OperatingSystem CURRENT = initOS();

    public static OperatingSystem current() {
        return CURRENT;
    }

    public static boolean isWindows() {
        return current() == WINDOWS;
    }

    public static boolean isLinux() {
        return current() == LINUX;
    }

    public static boolean isMacOS() {
        return current() == MACOS;
    }

    private static OperatingSystem initOS() {
        String name = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (name.startsWith("windows")) {
            return WINDOWS;
        }
        if (name.startsWith("mac")) {
            return MACOS;
        }
        if (name.startsWith("linux")) {
            return LINUX;
        }
        throw new IllegalStateException("Can't determine current OS: " + name);
    }
}
