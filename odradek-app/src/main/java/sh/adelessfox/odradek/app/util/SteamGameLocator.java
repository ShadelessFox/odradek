package sh.adelessfox.odradek.app.util;

import wtf.reversed.toolbox.util.SteamApps;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public final class SteamGameLocator implements GameLocator {
    @Override
    public Collection<Path> findAll() throws IOException {
        return SteamApps.findAllGames().values();
    }

    @Override
    public String name() {
        return "Steam";
    }
}
