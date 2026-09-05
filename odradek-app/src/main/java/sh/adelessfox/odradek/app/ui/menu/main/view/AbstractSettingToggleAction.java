package sh.adelessfox.odradek.app.ui.menu.main.view;

import sh.adelessfox.odradek.settings.Setting;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;

abstract class AbstractSettingToggleAction extends Action implements Action.Check {
    @Override
    public void perform(ActionContext context) {
        var setting = getSetting();
        setting.set(!setting.value());
    }

    @Override
    public boolean isChecked(ActionContext context) {
        return getSetting().value();
    }

    protected abstract Setting<Boolean> getSetting();
}
