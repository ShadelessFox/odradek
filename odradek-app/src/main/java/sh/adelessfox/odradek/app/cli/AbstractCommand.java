package sh.adelessfox.odradek.app.cli;

import picocli.CommandLine.Option;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.game.decima.DecimaGame;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

abstract class AbstractCommand implements Callable<Void> {
    @Option(names = {"-s", "--source"}, description = "Path to the game's root directory where its executable resides", required = true)
    private Path source;

    @Override
    public Void call() throws Exception {
        try (var game = createGame(source)) {
            execute(game);
            return null;
        }
    }

    abstract void execute(DecimaGame game) throws IOException;

    private DecimaGame createGame(Path path) throws IOException {
        return (DecimaGame) Game.load(path);
    }
}
