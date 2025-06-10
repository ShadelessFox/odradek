package sh.adelessfox.odradek.app;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import sh.adelessfox.odradek.app.menu.ActionIds;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.EPlatform;
import sh.adelessfox.odradek.ui.actions.Actions;

import javax.swing.*;
import java.nio.file.Path;

public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Command(name = "Odradek")
    private static class Options {
        @Option(names = {"-s", "--source"}, description = "Path to the game's root directory")
        private Path source;
    }

    public static void main(String[] args) throws Exception {
        FlatInspector.install("ctrl shift alt X");
        FlatUIDefaultsInspector.install("ctrl shift alt Y");
        FlatLightLaf.setup();

        Options options = CommandLine.populateCommand(new Options(), args);
        Path source = options.source;

        if (source == null) {
            source = chooseGameDirectory();
        }

        if (source == null) {
            log.debug("No source directory was provided, exiting");
            return;
        }

        log.info("Loading game assets");
        ForbiddenWestGame game = new ForbiddenWestGame(source, EPlatform.WinGame);

        log.info("Starting the application");
        start(source, game);
    }

    private static Path chooseGameDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose game directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().toPath();
        } else {
            return null;
        }
    }

    private static void start(Path source, ForbiddenWestGame game) {
        var frame = new JFrame();
        frame.add(new ApplicationWindow(game));
        frame.setJMenuBar(Actions.createMenuBar(ActionIds.MAIN_MENU_ID));
        frame.setTitle("Odradek - " + source);
        frame.setSize(1280, 720);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);

        JOptionPane.setRootFrame(frame);
    }
}
