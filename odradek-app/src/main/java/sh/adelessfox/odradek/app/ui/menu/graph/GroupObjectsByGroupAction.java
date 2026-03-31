package sh.adelessfox.odradek.app.ui.menu.graph;

import sh.adelessfox.odradek.app.ui.component.graph.GraphStructure;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.components.tree.StructuredTree;
import sh.adelessfox.odradek.ui.data.DataKeys;

import java.util.Objects;

@ActionRegistration(text = "Group objects by group")
@ActionContribution(parent = GraphMenu.ID)
public class GroupObjectsByGroupAction extends Action implements Action.Check {
    @Override
    public void perform(ActionContext context) {
        var tree = context.get(DataKeys.COMPONENT, StructuredTree.class).orElseThrow();
        var groupable = context.get(DataKeys.SELECTION, GraphStructure.GroupableByGroup.class).orElseThrow();

        var option = GraphStructure.GroupableByGroup.Option.GROUP_BY_GROUP;
        if (!groupable.options().remove(option)) {
            groupable.options().add(option);
        }
        tree.getModel().refresh(Objects.requireNonNull(tree.getSelectionPath()));
    }

    @Override
    public boolean isVisible(ActionContext context) {
        return context.has(DataKeys.SELECTION, GraphStructure.GroupableByGroup.class);
    }

    @Override
    public boolean isChecked(ActionContext context) {
        return context.get(DataKeys.SELECTION, GraphStructure.GroupableByGroup.class)
            .filter(g -> g.options().contains(GraphStructure.GroupableByGroup.Option.GROUP_BY_GROUP))
            .isPresent();
    }
}
