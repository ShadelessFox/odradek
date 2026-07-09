package sh.adelessfox.odradek.app.ui.menu.main.view;

import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.menu.MenuIds;
import sh.adelessfox.odradek.app.ui.menu.main.MainMenu;
import sh.adelessfox.odradek.app.ui.settings.Setting;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;

@ActionRegistration(text = "Show Object Type Information", description = "Enable tooltips that show information about the type of the hovered element in the object viewer")
@ActionContribution(parent = MainMenu.View.ID, group = MenuIds.GROUP_UTIL)
public final class ToggleShowObjectTypeInformationAction extends AbstractSettingToggleAction {
    @Override
    protected Setting<Boolean> getSetting() {
        return Application.getInstance().settings().showObjectTypeInformation();
    }
}
