package sh.adelessfox.odradek.app.ui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatSVGUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.ui.component.main.MainPresenter;
import sh.adelessfox.odradek.app.ui.component.main.MainView;
import sh.adelessfox.odradek.app.ui.menu.main.MainMenu;
import sh.adelessfox.odradek.app.ui.settings.ApplicationSettings;
import sh.adelessfox.odradek.data.DataContext;
import sh.adelessfox.odradek.event.DefaultEventBus;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.game.decima.DecimaGame;
import sh.adelessfox.odradek.service.ApplicationService;
import sh.adelessfox.odradek.settings.Settings;
import sh.adelessfox.odradek.settings.SettingsEvent;
import sh.adelessfox.odradek.settings.SettingsManager;
import sh.adelessfox.odradek.ui.actions.Actions;
import sh.adelessfox.odradek.ui.data.DataContexts;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.editors.stack.EditorStackManager;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
        run(game, params);
    }

    private static void run(DecimaGame game, ApplicationParameters params) {
        var events = new DefaultEventBus();
        var settings = new SettingsManager(params.configPath().resolve("settings.json"), events);

        var context = new DataContext();
        context.put(ApplicationDataKeys.CONFIG_PATH, params.configPath());
        context.put(ApplicationDataKeys.SETTINGS, settings);
        context.put(DataKeys.EVENT_BUS, events);
        context.put(DataKeys.GAME, game);

        var theme = settings.get(ApplicationSettings.THEME).value();
        FlatLaf.setup(theme.createLookAndFeel());
        FlatLaf.updateUI();

        var editors = new EditorStackManager(context);
        context.put(DataKeys.EDITOR_MANAGER, editors);

        var services = new ArrayList<ApplicationService>();
        ApplicationService.providers().forEach(provider -> services.add(provider.create(context)));

        var frame = new JFrame();
        var view = new MainView(editors, events, context);
        var presenter = new MainPresenter(editors, view, events, game);
        configureFrame(frame, presenter, context, events, params);
        frame.setVisible(true);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(settings, services, game), "Odradek Shutdown"));
    }

    private static void configureFrame(
        JFrame frame,
        MainPresenter presenter,
        DataContext context,
        EventBus events,
        ApplicationParameters params
    ) {
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
    }

    private static void shutdown(SettingsManager settings, List<ApplicationService> services, Game game) {
        try {
            settings.save();
        } catch (RuntimeException failure) {
            log.error("Failed to save settings", failure);
        } finally {
            dispose(settings, services, game);
        }
    }

    private static void dispose(SettingsManager settings, List<ApplicationService> services, Game game) {
        if (settings != null) {
            settings.close();
        }
        disposeServices(services);
        try {
            game.close();
        } catch (IOException failure) {
            log.error("Failed to close game", failure);
        }
    }

    static void disposeServices(List<ApplicationService> services) {
        for (var service : services) {
            try {
                service.dispose();
            } catch (RuntimeException failure) {
                log.error("Failed to dispose of service {}", service.getClass().getName(), failure);
            }
        }
        services.clear();
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
