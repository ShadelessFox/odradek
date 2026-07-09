package sh.adelessfox.odradek.app.ui.menu.main.view;

import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.menu.MenuIds;
import sh.adelessfox.odradek.app.ui.menu.main.MainMenu;
import sh.adelessfox.odradek.app.ui.settings.Setting;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;

@ActionRegistration(text = "Show Object Previews", description = "Enable previews when hovering over some objects in the navigator and object viewer")
@ActionContribution(parent = MainMenu.View.ID, group = MenuIds.GROUP_UTIL)
public final class ToggleShowObjectPreviewAction extends AbstractSettingToggleAction {
    @Override
    protected Setting<Boolean> getSetting() {
        return Application.getInstance().settings().showObjectPreview();
    }
}
