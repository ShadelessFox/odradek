package sh.adelessfox.odradek.app.ui.menu.graph;

import sh.adelessfox.odradek.app.ui.component.graph.GraphStructure.GroupableByGroup;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;

@ActionRegistration(text = "Sort by count")
@ActionContribution(parent = GraphMenu.ID)
public class SortGroupsByCountAction extends AbstractGroupableAction<GroupableByGroup, GroupableByGroup.Option> {
    @Override
    public boolean isVisible(ActionContext context) {
        return context.get(DataKeys.SELECTION, GroupableByGroup.class)
            .filter(objects -> objects.options().contains(GroupableByGroup.Option.GROUP_BY_GROUP))
            .isPresent();
    }

    @Override
    protected GroupableByGroup.Option getOption() {
        return GroupableByGroup.Option.SORT_BY_COUNT;
    }

    @Override
    protected Class<GroupableByGroup> getType() {
        return GroupableByGroup.class;
    }
}
