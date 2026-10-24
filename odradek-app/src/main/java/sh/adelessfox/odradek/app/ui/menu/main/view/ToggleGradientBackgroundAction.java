package sh.adelessfox.odradek.app.ui.menu.main.view;

import sh.adelessfox.odradek.app.ui.ApplicationDataKeys;
import sh.adelessfox.odradek.app.ui.component.main.MainEvent;
import sh.adelessfox.odradek.app.ui.menu.main.MainMenu;
import sh.adelessfox.odradek.app.ui.settings.ApplicationSettings;
import sh.adelessfox.odradek.settings.Setting;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;

@ActionRegistration(text = "&Gradient Background", description = "Use shared gradient across all panels")
@ActionContribution(parent = MainMenu.View.ID, order = 1)
public final class ToggleGradientBackgroundAction extends AbstractSettingToggleAction {
    @Override
    public void perform(ActionContext context) {
        var events = context.require(DataKeys.EVENT_BUS);
        super.perform(context);
        events.publish(new MainEvent.SetGradientBackground(isChecked(context)));
    }

    @Override
    protected Setting<Boolean> getSetting(ActionContext context) {
        return context.require(ApplicationDataKeys.SETTINGS).get(ApplicationSettings.GRADIENT_BACKGROUND);
    }
}
