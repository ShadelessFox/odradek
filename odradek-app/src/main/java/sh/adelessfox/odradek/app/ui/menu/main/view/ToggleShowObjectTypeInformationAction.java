package sh.adelessfox.odradek.app.ui.menu.main.view;

import sh.adelessfox.odradek.app.ui.ApplicationDataKeys;
import sh.adelessfox.odradek.app.ui.menu.MenuIds;
import sh.adelessfox.odradek.app.ui.menu.main.MainMenu;
import sh.adelessfox.odradek.app.ui.settings.ApplicationSettings;
import sh.adelessfox.odradek.settings.Setting;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;

@ActionRegistration(text = "Show Object Type Information", description = "Enable tooltips that show information about the type of the hovered element in the object viewer")
@ActionContribution(parent = MainMenu.View.ID, group = MenuIds.GROUP_UTIL)
public final class ToggleShowObjectTypeInformationAction extends AbstractSettingToggleAction {
    @Override
    protected Setting<Boolean> getSetting(ActionContext context) {
        return context.require(ApplicationDataKeys.SETTINGS).get(ApplicationSettings.SHOW_OBJECT_TYPE_INFORMATION);
    }
}
