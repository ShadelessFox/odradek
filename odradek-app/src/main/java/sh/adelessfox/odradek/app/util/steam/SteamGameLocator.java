package sh.adelessfox.odradek.app.util.steam;

import sh.adelessfox.odradek.app.util.GameLocator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class SteamGameLocator implements GameLocator {
    @Override
    public List<Path> findAll() throws IOException {
        return Steam.findAllGames();
    }

    @Override
    public String name() {
        return "Steam";
    }
}
