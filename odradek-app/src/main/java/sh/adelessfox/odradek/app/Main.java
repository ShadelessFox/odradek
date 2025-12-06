package sh.adelessfox.odradek.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import sh.adelessfox.odradek.app.cli.ExportAssetCommand;
import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.ApplicationParameters;
import sh.adelessfox.odradek.game.ObjectId;

import javax.swing.*;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(
    name = "odradek",
    version = "0.1",
    subcommands = {
        ExportAssetCommand.class
    },
    mixinStandardHelpOptions = true
)
public class Main implements Callable<Void> {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    @Option(names = {"-s", "--source"}, description = "Path to the game's root directory where its executable resides")
    private Path source;

    @Option(names = {"--dark"}, description = "Use dark theme for the UI")
    private boolean darkTheme = false;

    @Option(names = {"--debug"}, description = "Enable debug mode for the UI that features various inspectors")
    private boolean debugMode = false;

    static void main(String[] args) {
        new CommandLine(Main.class)
            .registerConverter(ObjectId.class, ObjectId::valueOf)
            .execute(args);
    }

    @Override
    public Void call() throws Exception {
        Path source = this.source;
        if (source == null) {
            source = chooseGameDirectory();
        }
        if (source != null) {
            new Application().launch(new ApplicationParameters(
                source,
                darkTheme,
                debugMode
            ));
        } else {
            log.info("No source directory was provided, exiting");
        }
        return null;
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
}
