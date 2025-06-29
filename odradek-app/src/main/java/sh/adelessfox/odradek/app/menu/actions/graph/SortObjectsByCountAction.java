package sh.adelessfox.odradek.app.menu.actions.graph;

import sh.adelessfox.odradek.app.GraphStructure.GroupObjects;
import sh.adelessfox.odradek.app.menu.ActionIds;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;

@ActionRegistration(text = "Sort by count")
@ActionContribution(parent = ActionIds.GRAPH_MENU_ID, order = 2)
public class SortObjectsByCountAction extends AbstractObjectsOptionAction {
    @Override
    public boolean isVisible(ActionContext context) {
        return context.get(DataKeys.SELECTION, GroupObjects.class)
            .filter(objects -> objects.options().contains(GroupObjects.Option.GROUP_BY_TYPE))
            .isPresent();
    }

    @Override
    protected GroupObjects.Option getOption() {
        return GroupObjects.Option.SORT_BY_COUNT;
    }
}
