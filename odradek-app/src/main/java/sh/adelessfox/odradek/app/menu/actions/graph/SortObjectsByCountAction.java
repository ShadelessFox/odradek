package sh.adelessfox.odradek.app.menu.actions.graph;

import sh.adelessfox.odradek.app.GraphStructure;
import sh.adelessfox.odradek.app.menu.ActionIds;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.tree.StructuredTree;

import java.util.Objects;

@ActionRegistration(id = "sh.adelessfox.odradek.app.menu.actions.graph.SortObjectsByCountAction", name = "Sort by count")
@ActionContribution(parent = ActionIds.GRAPH_MENU_ID, order = 2)
public class SortObjectsByCountAction extends Action {
    @Override
    public void perform(ActionContext context) {
        var tree = (StructuredTree<?>) context.getData(DataKeys.COMPONENT).orElseThrow();
        var objects = (GraphStructure.GroupObjects) context.getData(DataKeys.SELECTION).orElseThrow();
        var options = objects.options();

        if (options.contains(GraphStructure.GroupObjects.Options.SORT_BY_COUNT)) {
            options.remove(GraphStructure.GroupObjects.Options.SORT_BY_COUNT);
        } else {
            options.add(GraphStructure.GroupObjects.Options.SORT_BY_COUNT);
        }

        tree.getModel().update(Objects.requireNonNull(tree.getSelectionPath()));
    }

    @Override
    public boolean isVisible(ActionContext context) {
        Object selection = context.getData(DataKeys.SELECTION).orElse(null);
        return selection instanceof GraphStructure.GroupObjects objects
            && objects.options().contains(GraphStructure.GroupObjects.Options.GROUP_BY_TYPE);
    }
}
