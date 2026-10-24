package sh.adelessfox.odradek.app;

import picocli.CommandLine;

import java.time.format.DateTimeFormatter;

public final class BuildVersionProvider implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() {
        return new String[]{
            "Odradek " + BuildInfo.version(),
            "Upstream: " + BuildInfo.upstreamUrl(),
            "Commit: " + BuildInfo.commitHash().orElse("unknown"),
            "Built on " + BuildInfo.buildDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        };
    }
}
