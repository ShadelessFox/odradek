package sh.adelessfox.odradek.app.menu.actions.tabs;

import sh.adelessfox.odradek.app.menu.ActionIds;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;

import javax.swing.*;

@ActionRegistration(name = "Close &All Tabs")
@ActionContribution(parent = ActionIds.TABS_MENU_ID)
public class CloseAllTabsAction extends Action {
    @Override
    public void perform(ActionContext context) {
        context.getData(DataKeys.COMPONENT)
            .map(JTabbedPane.class::cast)
            .ifPresent(JTabbedPane::removeAll);
    }
}
