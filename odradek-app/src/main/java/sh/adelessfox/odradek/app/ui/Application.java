package sh.adelessfox.odradek.app.ui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatSVGUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.ui.bookmarks.BookmarkDataKeys;
import sh.adelessfox.odradek.app.ui.bookmarks.BookmarkPersistence;
import sh.adelessfox.odradek.app.ui.bookmarks.Bookmarks;
import sh.adelessfox.odradek.app.ui.component.main.MainPresenter;
import sh.adelessfox.odradek.app.ui.component.main.MainView;
import sh.adelessfox.odradek.app.ui.menu.main.MainMenu;
import sh.adelessfox.odradek.app.ui.settings.ApplicationSettings;
import sh.adelessfox.odradek.data.DataContext;
import sh.adelessfox.odradek.event.DefaultEventBus;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.game.decima.DecimaGame;
import sh.adelessfox.odradek.settings.Settings;
import sh.adelessfox.odradek.settings.SettingsEvent;
import sh.adelessfox.odradek.settings.SettingsManager;
import sh.adelessfox.odradek.ui.actions.Actions;
import sh.adelessfox.odradek.ui.data.DataContexts;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.editors.stack.EditorStackManager;
import sh.adelessfox.odradek.ui.util.Dialogs;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public final class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);
    private static boolean started;

    private Application() {
    }

    public static synchronized void start(ApplicationParameters params) throws IOException {
        if (started) {
            throw new IllegalStateException("Application is already running");
        }

        var game = (DecimaGame) Game.load(params.sourcePath());
        started = true;

        log.info("Starting the application");
        SwingUtilities.invokeLater(() -> run(game, params));
    }

    private static void run(DecimaGame game, ApplicationParameters params) {
        Thread.setDefaultUncaughtExceptionHandler((_, e) -> {
            log.error("Uncaught exception in thread '{}'", Thread.currentThread().getName(), e);
            Dialogs.showExceptionDialog(JOptionPane.getRootFrame(), e.toString(), e);
        });

        // Settings publishes a sticky AfterLoad event. Bind all services before creating
        // UI subscribers, which may restore panels/editors synchronously on subscription.
        var events = new DefaultEventBus();
        var settings = new SettingsManager(params.configPath().resolve("settings.json"), events);
        var theme = settings.get(ApplicationSettings.THEME).value();
        FlatLaf.setup(theme.createLookAndFeel());
        FlatLaf.updateUI();
        var context = initializeContext(game, params.configPath(), events, settings);
        var editors = context.require(DataKeys.EDITOR_MANAGER);
        var view = new MainView(editors, events, context);
        var presenter = new MainPresenter(editors, view, events, game);

        var frame = new JFrame();
        DataContexts.putDataContext(frame.getRootPane(), context);
        frame.add(presenter.getRoot());
        frame.setTitle("Odradek - " + params.sourcePath());
        frame.setIconImages(FlatSVGUtils.createWindowIconImages(Application.class.getResource("application.svg")));
        frame.setSize(1280, 720);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Actions.installMenuBar(frame.getRootPane(), MainMenu.ID, DataContexts.focusedComponent(context));
        JOptionPane.setRootFrame(frame);

        events.subscribe(SettingsEvent.class, event -> {
            switch (event) {
                case SettingsEvent.AfterLoad(var state) -> loadFrameSettings(state, frame);
                case SettingsEvent.BeforeSave(var state) -> saveFrameSettings(state, frame);
            }
        });

        // And now we can show the frame
        frame.setVisible(true);
    }

    /** Creates shared services and completes their bindings on the EDT before constructing the main UI. */
    static DataContext initializeContext(DecimaGame game, Path config, EventBus events, Settings settings) {
        var context = new DataContext();
        context.put(DataKeys.GAME, game);
        context.put(ApplicationDataKeys.CONFIG_PATH, config);
        context.put(DataKeys.EVENT_BUS, events);
        context.put(ApplicationDataKeys.SETTINGS, settings);
        var bookmarks = new Bookmarks(events);
        context.put(BookmarkDataKeys.BOOKMARKS, bookmarks);
        context.put(DataKeys.EDITOR_MANAGER, new EditorStackManager(context));
        new BookmarkPersistence(bookmarks, settings, events);
        return context;
    }

    private static void loadFrameSettings(Settings settings, JFrame frame) {
        settings.get(ApplicationSettings.WINDOW).value().ifPresent(window -> {
            if (window.maximized()) {
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            } else {
                frame.setBounds(window.x(), window.y(), window.width(), window.height());
            }
        });
    }

    private static void saveFrameSettings(Settings settings, JFrame frame) {
        var bounds = frame.getBounds();
        var maximized = (frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
        var state = new ApplicationSettings.WindowState(bounds.x, bounds.y, bounds.width, bounds.height, maximized);
        settings.get(ApplicationSettings.WINDOW).set(Optional.of(state));
    }
}
