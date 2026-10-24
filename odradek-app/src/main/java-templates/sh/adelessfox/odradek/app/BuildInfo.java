package sh.adelessfox.odradek.app;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

public final class BuildInfo {
    private static final LocalDateTime BUILD_DATE = LocalDateTime.parse("${build.date}");
    private static final String VERSION = "${project.version}";
    private static final String COMMIT_HASH = "${git.commit.id.full}";
    private static final URI UPSTREAM_URL = URI.create("${project.url}");

    private BuildInfo() {
    }

    /** The UTC date and time of the Maven build. */
    public static LocalDateTime buildDate() {
        return BUILD_DATE;
    }

    /** The version of the application, as specified in the Maven POM. */
    public static String version() {
        return VERSION;
    }

    /** The full Git commit hash, or {@code Optional.empty()} when building without Git metadata. */
    public static Optional<String> commitHash() {
        return COMMIT_HASH.equals("unknown") ? Optional.empty() : Optional.of(COMMIT_HASH);
    }

    public static URI upstreamUrl() {
        return UPSTREAM_URL;
    }
}
