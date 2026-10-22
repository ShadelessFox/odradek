package sh.adelessfox.odradek.app.ui.menu.main.view;

import sh.adelessfox.odradek.app.ui.ApplicationDataKeys;
import sh.adelessfox.odradek.app.ui.menu.main.MainMenu;
import sh.adelessfox.odradek.app.ui.settings.ApplicationSettings;
import sh.adelessfox.odradek.settings.Setting;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.actions.MenuIds;

@ActionRegistration(text = "Show Object Previews", description = "Enable previews when hovering over some objects in the navigator and object viewer")
@ActionContribution(parent = MainMenu.View.ID, group = MenuIds.GROUP_UTIL)
public final class ToggleShowObjectPreviewAction extends AbstractSettingToggleAction {
    @Override
    protected Setting<Boolean> getSetting(ActionContext context) {
        return context.require(ApplicationDataKeys.SETTINGS).get(ApplicationSettings.SHOW_OBJECT_PREVIEW);
    }
}
