package sh.adelessfox.odradek.app.ui.tools.graph.menu;

import sh.adelessfox.odradek.app.ui.tools.graph.GraphStructure.GroupableByGroup;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.actions.MenuIds;
import sh.adelessfox.odradek.ui.data.DataKeys;

@ActionRegistration(text = "Group objects by group")
@ActionContribution(parent = MenuIds.GRAPH_MENU)
public class GroupObjectsByGroupAction extends AbstractGroupableAction<GroupableByGroup, GroupableByGroup.Option> {
    @Override
    public boolean isVisible(ActionContext context) {
        return context.has(DataKeys.SELECTION, GroupableByGroup.class);
    }

    @Override
    protected GroupableByGroup.Option getOption() {
        return GroupableByGroup.Option.GROUP_BY_GROUP;
    }

    @Override
    protected Class<GroupableByGroup> getType() {
        return GroupableByGroup.class;
    }
}
