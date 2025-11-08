package sh.adelessfox.odradek.app.ui.menu.graph;

import sh.adelessfox.odradek.app.ui.component.graph.GraphStructure.GroupableByType;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;

@ActionRegistration(text = "Sort by count")
@ActionContribution(parent = GraphMenu.ID, order = 2)
public class SortObjectsByCountAction extends AbstractObjectsOptionAction {
    @Override
    public boolean isVisible(ActionContext context) {
        return context.get(DataKeys.SELECTION, GroupableByType.class)
            .filter(objects -> objects.options().contains(GroupableByType.Option.GROUP_BY_TYPE))
            .isPresent();
    }

    @Override
    protected GroupableByType.Option getOption() {
        return GroupableByType.Option.SORT_BY_COUNT;
    }
}
