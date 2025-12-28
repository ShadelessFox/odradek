package sh.adelessfox.odradek.app.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.ui.bookmarks.BookmarkRepository;
import sh.adelessfox.odradek.app.ui.menu.main.MainMenu;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.EPlatform;
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

    @SuppressWarnings("resource")
    public static void launch(ApplicationParameters params) throws IOException {
        if (application != null) {
            throw new IllegalStateException("Application is already running");
        }

        var game = new ForbiddenWestGame(params.sourcePath(), EPlatform.WinGame);

        log.info("Starting the application");
        SwingUtilities.invokeLater(() -> {
            if (params.enableDebugMode()) {
                FlatInspector.install("ctrl shift alt X");
                FlatUIDefaultsInspector.install("ctrl shift alt Y");
            }

            if (params.enableDarkTheme()) {
                FlatDarkLaf.setup();
            } else {
                FlatLightLaf.setup();
            }

            // Instantiate components here. Some objects, notably EditorManager,
            // requires the UI to be completely set up so the LaF is not fucked.
            // The loading order drives me crazy. Maybe I should get rid of Dagger completely?
            ApplicationComponent component = DaggerApplicationComponent.builder()
                .game(game)
                .build();

            application = new Application(component, params);

            var frame = new JFrame();
            Actions.installMenuBar(frame.getRootPane(), MainMenu.ID, DataContext.focusedComponent());
            frame.add(component.presenter().getRoot());
            frame.setTitle("Odradek - " + params.sourcePath());
            frame.setSize(1280, 720);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setVisible(true);

            JOptionPane.setRootFrame(frame);

            // Ensure settings are loaded after everything else. Seems hacky
            component.settings();
        });
    }

    public ForbiddenWestGame game() {
        return component.game();
    }

    public EditorManager editors() {
        return component.editorManager();
    }

    public BookmarkRepository bookmarks() {
        return component.bookmarkRepository();
    }

    public boolean isDebugMode() {
        return parameters.enableDebugMode();
    }
}
