package sh.adelessfox.odradek.app.ui.menu.main.view;

import sh.adelessfox.odradek.data.DataContext;
import sh.adelessfox.odradek.settings.Setting;
import sh.adelessfox.odradek.ui.actions.Action;

abstract class AbstractSettingToggleAction extends Action implements Action.Check {
    @Override
    public void perform(DataContext context) {
        var setting = getSetting(context);
        setting.set(!setting.value());
    }

    @Override
    public boolean isChecked(DataContext context) {
        return getSetting(context).value();
    }

    protected abstract Setting<Boolean> getSetting(DataContext context);
}
