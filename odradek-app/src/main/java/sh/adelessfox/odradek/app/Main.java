package sh.adelessfox.odradek.app;

import com.sun.tools.attach.VirtualMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import sh.adelessfox.odradek.app.cli.ExportAssetCommand;
import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.ApplicationParameters;
import sh.adelessfox.odradek.game.ObjectId;
import sh.adelessfox.odradek.util.system.OperatingSystem;

import javax.swing.*;
import java.nio.file.Path;
import java.util.Optional;
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
    private Optional<Path> source;

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
        ensureSingleRunningInstance(getClass());

        var configPath = determineConfigPath("Odradek");
        var sourcePath = source.or(Main::chooseGameDirectory).orElse(null);

        if (sourcePath == null) {
            log.info("No source directory was provided, exiting");
            System.exit(1);
        }

        Application.start(new ApplicationParameters(
            configPath,
            sourcePath,
            darkTheme,
            debugMode
        ));

        return null;
    }

    private static void ensureSingleRunningInstance(Class<?> cls) {
        var name = cls.getModule().getName() + '/' + cls.getName();
        var vms = VirtualMachine.list().stream()
            .filter(vm -> vm.displayName().startsWith(name))
            .toList();
        if (vms.size() > 1) {
            log.error("Another instance of app is already running, exiting");
            JOptionPane.showMessageDialog(
                JOptionPane.getRootFrame(),
                "Another instance of the application is already running, please close it first",
                "Odradek",
                JOptionPane.INFORMATION_MESSAGE);
            System.exit(1);
        }
    }

    private static Path determineConfigPath(String identifier) {
        String userHome = System.getProperty("user.home");
        if (userHome == null) {
            throw new IllegalStateException("Unable to determine user home directory");
        }
        return switch (OperatingSystem.name()) {
            case WINDOWS -> Path.of(userHome, "AppData", "Local", identifier);
            case MACOS -> Path.of(userHome, "Library", "Application Support", identifier);
            case LINUX -> Path.of(userHome, ".config", identifier);
        };
    }

    private static Optional<Path> chooseGameDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose game directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return Optional.of(chooser.getSelectedFile().toPath());
        } else {
            return Optional.empty();
        }
    }
}
