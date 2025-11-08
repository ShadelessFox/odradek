package sh.adelessfox.odradek.app.cli;

import picocli.CommandLine.Option;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

abstract class AbstractCommand implements Callable<Void> {
    @Option(names = {"-s", "--source"}, description = "Path to the game's root directory where its executable resides", required = true)
    private Path source;

    @Override
    public Void call() throws Exception {
        try (Game game = createGame(source)) {
            execute(game);
            return null;
        }
    }

    abstract void execute(Game game) throws IOException;

    private Game createGame(Path source) throws IOException {
        // For the dear future me: this should be game agnostic
        return new ForbiddenWestGame(source, HorizonForbiddenWest.EPlatform.WinGame);
    }
}
