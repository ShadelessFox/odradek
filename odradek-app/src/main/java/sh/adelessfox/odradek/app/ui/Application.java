package sh.adelessfox.odradek.app.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.cli.data.ObjectId;
import sh.adelessfox.odradek.app.ui.menu.main.MainMenu;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.EPlatform;
import sh.adelessfox.odradek.ui.actions.Actions;
import sh.adelessfox.odradek.ui.data.DataContext;

import javax.swing.*;
import java.io.IOException;
import java.util.Optional;

public final class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public void launch(ApplicationParameters params) throws IOException {
        log.info("Loading game assets");
        ForbiddenWestGame game = new ForbiddenWestGame(params.sourcePath(), EPlatform.WinGame);

        log.info("Starting the application");

        ApplicationComponent component = DaggerApplicationComponent.builder()
            .game(game)
            .build();

        DataContext context = DataContext.focusedComponent().or(key -> {
            if (ApplicationKeys.MAIN_PRESENTER.is(key)) {
                return Optional.of(component.presenter());
            }
            return Optional.empty();
        });

        SwingUtilities.invokeLater(() -> {
            if (params.enableDebugMode()) {
                UIManager.put(ApplicationKeys.DEBUG_MODE, Boolean.TRUE);
                FlatInspector.install("ctrl shift alt X");
                FlatUIDefaultsInspector.install("ctrl shift alt Y");
            }

            if (params.enableDarkTheme()) {
                FlatDarkLaf.setup();
            } else {
                FlatLightLaf.setup();
            }

            var frame = new JFrame();
            Actions.installMenuBar(frame.getRootPane(), MainMenu.ID, context);
            frame.add(component.presenter().getRoot());
            frame.setTitle("Odradek - " + params.sourcePath());
            frame.setSize(1280, 720);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setVisible(true);

            JOptionPane.setRootFrame(frame);

            for (ObjectId object : params.objectsToOpen()) {
                component.presenter().showObject(object.groupId(), object.objectIndex());
            }
        });
    }
}
