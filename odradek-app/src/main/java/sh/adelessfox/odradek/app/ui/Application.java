package sh.adelessfox.odradek.app.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.ui.bookmarks.Bookmarks;
import sh.adelessfox.odradek.app.ui.menu.main.MainMenu;
import sh.adelessfox.odradek.app.ui.settings.Settings;
import sh.adelessfox.odradek.app.ui.settings.SettingsEvent;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.ui.actions.Actions;
import sh.adelessfox.odradek.ui.data.DataContext;
import sh.adelessfox.odradek.ui.editors.EditorManager;

import javax.swing.*;
import java.io.IOException;

public final class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);
    private static Application application;

    private final ApplicationComponent component;
    private final ApplicationParameters parameters;

    private Application(ApplicationComponent component, ApplicationParameters parameters) {
        this.component = component;
        this.parameters = parameters;
    }

    public static Application getInstance() {
        if (application == null) {
            throw new IllegalStateException("Application is not running");
        }
        return application;
    }

    public static synchronized void start(ApplicationParameters params) throws IOException {
        if (application != null) {
            throw new IllegalStateException("Application is already running");
        }

        var game = (ForbiddenWestGame) Game.load(params.sourcePath());
        var component = DaggerApplicationComponent.builder()
            .game(game)
            .build();

        application = new Application(component, params);

        log.info("Starting the application");
        SwingUtilities.invokeLater(() -> run(component, params));
    }

    private static void run(ApplicationComponent component, ApplicationParameters params) {
        if (params.enableDebugMode()) {
            FlatInspector.install("ctrl shift alt X");
            FlatUIDefaultsInspector.install("ctrl shift alt Y");
        }

        if (params.enableDarkTheme()) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }

        var frame = new JFrame();
        frame.add(component.presenter().getRoot());
        frame.setTitle("Odradek - " + params.sourcePath());
        frame.setSize(1280, 720);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Actions.installMenuBar(frame.getRootPane(), MainMenu.ID, DataContext.focusedComponent());
        JOptionPane.setRootFrame(frame);

        component.events().subscribe(SettingsEvent.class, event -> {
            switch (event) {
                case SettingsEvent.AfterLoad(var settings) -> loadFrameSettings(settings, frame);
                case SettingsEvent.BeforeSave(var settings) -> saveFrameSettings(settings, frame);
            }
        });

        // Ensure settings are initialized and loaded after everything else
        component.settings();

        // And now we can show the frame
        frame.setVisible(true);
    }

    private static void loadFrameSettings(Settings settings, JFrame frame) {
        settings.window().ifPresent(window -> {
            if (window.maximized()) {
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            } else {
                frame.setBounds(window.x(), window.y(), window.width(), window.height());
            }
        });
    }

    private static void saveFrameSettings(Settings settings, JFrame frame) {
        var state = frame.getExtendedState();
        var bounds = frame.getBounds();
        var maximized = (state & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;

        settings.window().set(new Settings.WindowState(
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height,
            maximized
        ));
    }

    public ForbiddenWestGame game() {
        return component.game();
    }

    public EditorManager editors() {
        return component.editors();
    }

    public Bookmarks bookmarks() {
        return component.bookmarks();
    }

    public boolean isDebugMode() {
        return parameters.enableDebugMode();
    }
}
