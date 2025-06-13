package sh.adelessfox.odradek.app.menu.actions.graph;

import sh.adelessfox.odradek.Gatherers;
import sh.adelessfox.odradek.app.GraphStructure;
import sh.adelessfox.odradek.app.menu.ActionIds;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;

@ActionRegistration(name = "Sort by count")
@ActionContribution(parent = ActionIds.GRAPH_MENU_ID, order = 2)
public class SortObjectsByCountAction extends AbstractObjectsOptionAction {
    @Override
    public boolean isVisible(ActionContext context) {
        return context.getData(DataKeys.SELECTION).stream()
            .gather(Gatherers.instanceOf(GraphStructure.GroupObjects.class))
            .anyMatch(objects -> objects.options().contains(GraphStructure.GroupObjects.Option.GROUP_BY_TYPE));
    }

    @Override
    protected GraphStructure.GroupObjects.Option getOption() {
        return GraphStructure.GroupObjects.Option.SORT_BY_COUNT;
    }
}
