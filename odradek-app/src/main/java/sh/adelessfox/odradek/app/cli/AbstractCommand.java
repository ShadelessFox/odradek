package sh.adelessfox.odradek.app.cli;

import picocli.CommandLine.Option;
import sh.adelessfox.odradek.game.Game;

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

    private Game createGame(Path path) throws IOException {
        return Game.load(path);
    }
}
