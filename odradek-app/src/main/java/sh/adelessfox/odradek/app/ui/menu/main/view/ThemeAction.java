package sh.adelessfox.odradek.app.ui.menu.main.view;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.menu.main.MainMenu;
import sh.adelessfox.odradek.app.ui.settings.Settings;
import sh.adelessfox.odradek.ui.actions.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ActionRegistration(id = ThemeAction.ID, text = "Theme")
@ActionContribution(parent = MainMenu.View.ID)
public final class ThemeAction extends Action {
    public static final String ID = "sh.adelessfox.odradek.app.ui.menu.main.view.ChangeThemeAction";

    @ActionRegistration(text = "")
    @ActionContribution(parent = ThemeAction.ID)
    public static final class Placeholder extends Action implements ActionProvider {
        @Override
        public List<? extends Action> create(ActionContext context) {
            return Stream.of(Settings.Theme.values())
                .map(ChangeThemeAction::new)
                .toList();
        }
    }

    private static final class ChangeThemeAction extends Action implements Action.Radio {
        private final Settings.Theme theme;

        ChangeThemeAction(Settings.Theme theme) {
            this.theme = theme;
        }

        @Override
        public void perform(ActionContext context) {
            if (isSelected(context)) {
                return;
            }

            FlatAnimatedLafChange.showSnapshot();
            FlatLaf.setup(theme.createLookAndFeel());
            FlatLaf.updateUI();
            FlatAnimatedLafChange.hideSnapshotWithAnimation();

            var setting = Application.getInstance().settings().theme();
            setting.set(theme);
        }

        @Override
        public Optional<String> getText(ActionContext context) {
            return Optional.of(theme.toString());
        }

        @Override
        public boolean isSelected(ActionContext context) {
            var setting = Application.getInstance().settings().theme();
            return setting.get().orElse(null) == theme;
        }
    }
}
