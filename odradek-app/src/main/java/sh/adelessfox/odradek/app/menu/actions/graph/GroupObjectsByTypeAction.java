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

@ActionRegistration(id = "sh.adelessfox.odradek.app.menu.actions.graph.GroupObjectsByTypeAction", name = "Group objects by type")
@ActionContribution(parent = ActionIds.GRAPH_MENU_ID, order = 1)
public class GroupObjectsByTypeAction extends Action {
    @Override
    public void perform(ActionContext context) {
        var tree = (StructuredTree<?>) context.getData(DataKeys.COMPONENT).orElseThrow();
        var objects = (GraphStructure.GroupObjects) context.getData(DataKeys.SELECTION).orElseThrow();
        var options = objects.options();

        if (options.contains(GraphStructure.GroupObjects.Options.GROUP_BY_TYPE)) {
            options.remove(GraphStructure.GroupObjects.Options.GROUP_BY_TYPE);
        } else {
            options.add(GraphStructure.GroupObjects.Options.GROUP_BY_TYPE);
        }

        tree.getModel().update(Objects.requireNonNull(tree.getSelectionPath()));
    }

    @Override
    public boolean isVisible(ActionContext context) {
        Object selection = context.getData(DataKeys.SELECTION).orElse(null);
        return selection instanceof GraphStructure.GroupObjects;
    }
}
