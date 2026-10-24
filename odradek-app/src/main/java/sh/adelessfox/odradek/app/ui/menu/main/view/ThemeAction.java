package sh.adelessfox.odradek.app.ui.menu.main.view;

import sh.adelessfox.odradek.app.ui.ApplicationDataKeys;
import sh.adelessfox.odradek.app.ui.component.main.MainEvent;
import sh.adelessfox.odradek.app.ui.menu.main.MainMenu;
import sh.adelessfox.odradek.app.ui.settings.ApplicationSettings;
import sh.adelessfox.odradek.ui.actions.*;
import sh.adelessfox.odradek.ui.data.DataKeys;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ActionRegistration(id = ThemeAction.ID, text = "Theme")
@ActionContribution(parent = MainMenu.View.ID, order = 0)
public final class ThemeAction extends Action {
    public static final String ID = "sh.adelessfox.odradek.app.ui.menu.main.view.ChangeThemeAction";

    @ActionRegistration(text = "")
    @ActionContribution(parent = ThemeAction.ID)
    public static final class Placeholder extends Action implements ActionProvider {
        @Override
        public List<? extends Action> create(ActionContext context) {
            return Stream.of(ApplicationSettings.Theme.values())
                .map(ChangeThemeAction::new)
                .toList();
        }
    }

    private static final class ChangeThemeAction extends Action implements Action.Radio {
        private final ApplicationSettings.Theme theme;

        ChangeThemeAction(ApplicationSettings.Theme theme) {
            this.theme = theme;
        }

        @Override
        public void perform(ActionContext context) {
            if (isSelected(context)) {
                return;
            }
            context.require(DataKeys.EVENT_BUS).publish(new MainEvent.SetTheme(theme));
        }

        @Override
        public Optional<String> getText(ActionContext context) {
            return Optional.of(theme.toString());
        }

        @Override
        public boolean isSelected(ActionContext context) {
            var setting = context.require(ApplicationDataKeys.SETTINGS).get(ApplicationSettings.THEME);
            return setting.value() == theme;
        }
    }
}
