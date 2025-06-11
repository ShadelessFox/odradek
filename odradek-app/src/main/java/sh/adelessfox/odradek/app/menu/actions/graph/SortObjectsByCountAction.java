package sh.adelessfox.odradek.app.menu.actions.graph;

import sh.adelessfox.odradek.app.GraphStructure;
import sh.adelessfox.odradek.app.menu.ActionIds;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;

@ActionRegistration(id = "sh.adelessfox.odradek.app.menu.actions.graph.SortObjectsByCountAction", name = "Sort by count")
@ActionContribution(parent = ActionIds.GRAPH_MENU_ID, order = 2)
public class SortObjectsByCountAction extends AbstractObjectsOptionAction {
    @Override
    public boolean isVisible(ActionContext context) {
        Object selection = context.getData(DataKeys.SELECTION).orElse(null);
        return selection instanceof GraphStructure.GroupObjects objects
            && objects.options().contains(GraphStructure.GroupObjects.Options.GROUP_BY_TYPE);
    }

    @Override
    protected GraphStructure.GroupObjects.Options getOption() {
        return GraphStructure.GroupObjects.Options.SORT_BY_COUNT;
    }
}
