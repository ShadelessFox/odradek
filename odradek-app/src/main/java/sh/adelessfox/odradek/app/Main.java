package sh.adelessfox.odradek.app;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import com.sun.tools.attach.VirtualMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import sh.adelessfox.odradek.app.cli.ExportAssetCommand;
import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.ApplicationParameters;
import sh.adelessfox.odradek.app.ui.component.browser.BrowserDialog;
import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.util.system.OperatingSystem;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

@Command(
    name = "odradek",
    version = "0.1",
    subcommands = {
        ExportAssetCommand.class
    },
    mixinStandardHelpOptions = true
)
public class Main implements Runnable {
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
    public void run() {
        SwingUtilities.invokeLater(this::start);
    }

    private void start() {
        setupUI();
        ensureSingleRunningInstance(getClass());

        var configPath = determineConfigPath("Odradek");
        var sourcePath = source.or(Main::chooseGameDirectory).orElse(null);

        if (sourcePath == null) {
            log.info("No source directory was provided, exiting");
            System.exit(1);
        }

        try {
            Application.start(new ApplicationParameters(configPath, sourcePath, debugMode));
        } catch (IOException e) {
            log.error("Failed to start the application", e);
            JOptionPane.showMessageDialog(
                null,
                "Failed to start the application: " + e.getMessage(),
                "Odradek",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupUI() {
        if (OperatingSystem.name() == OperatingSystem.Name.LINUX) {
            // enable custom window decorations
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
        }

        if (debugMode) {
            FlatInspector.install("ctrl shift alt X");
            FlatUIDefaultsInspector.install("ctrl shift alt Y");
        }

        if (darkTheme) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
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
        var dialog = new BrowserDialog();
        dialog.setTitle("Choose game directory");
        dialog.setSize(550, 300);
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);

        return dialog.getPath();
    }
}
