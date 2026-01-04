package sh.adelessfox.odradek.util;

import java.util.Locale;

/**
 * Utility class for determining the current operating system and architecture.
 * <p>
 * Vaguely based on {@code com.formdev.flatlaf.util.SystemInfo}
 */
public final class OS {
    public enum Name {
        WINDOWS,
        LINUX,
        MACOS
    }

    public enum Arch {
        AMD64,
        AARCH64,
        X86
    }

    private static final Name name;
    private static final Arch arch;

    static {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        String osArch = System.getProperty("os.arch").toLowerCase(Locale.ENGLISH);

        if (osName.startsWith("windows")) {
            name = Name.WINDOWS;
        } else if (osName.startsWith("mac")) {
            name = Name.MACOS;
        } else if (osName.startsWith("linux")) {
            name = Name.LINUX;
        } else {
            throw new IllegalStateException("Can't determine current OS: " + osName);
        }

        arch = switch (osArch) {
            case "amd64", "x86_64" -> Arch.AMD64;
            case "aarch64" -> Arch.AARCH64;
            case "x86" -> Arch.X86;
            default -> throw new IllegalStateException("Can't determine current architecture: " + osArch);
        };
    }

    private OS() {
    }

    public static Name name() {
        return name;
    }

    public static Arch arch() {
        return arch;
    }
}
