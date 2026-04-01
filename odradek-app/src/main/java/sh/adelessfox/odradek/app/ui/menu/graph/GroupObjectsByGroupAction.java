package sh.adelessfox.odradek.app.ui.menu.graph;

import sh.adelessfox.odradek.app.ui.component.graph.GraphStructure.GroupableByGroup;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;

@ActionRegistration(text = "Group objects by group")
@ActionContribution(parent = GraphMenu.ID)
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
