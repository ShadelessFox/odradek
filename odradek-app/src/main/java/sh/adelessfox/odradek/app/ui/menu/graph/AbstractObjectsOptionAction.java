package sh.adelessfox.odradek.app.ui.menu.graph;

import sh.adelessfox.odradek.app.ui.component.graph.GraphStructure.GroupableByType;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.components.tree.StructuredTree;
import sh.adelessfox.odradek.ui.data.DataKeys;

import java.util.Objects;

abstract class AbstractObjectsOptionAction extends Action implements Action.Check {
    @Override
    public void perform(ActionContext context) {
        var tree = context.get(DataKeys.COMPONENT, StructuredTree.class).orElseThrow();
        var groupable = context.get(DataKeys.SELECTION, GroupableByType.class).orElseThrow();

        var option = getOption();
        var options = groupable.options();
        if (!options.remove(option)) {
            options.add(option);
        }

        tree.getModel().update(Objects.requireNonNull(tree.getSelectionPath()));
    }

    @Override
    public boolean isVisible(ActionContext context) {
        return context.get(DataKeys.SELECTION, GroupableByType.class).isPresent();
    }

    @Override
    public boolean isChecked(ActionContext context) {
        return context.get(DataKeys.SELECTION, GroupableByType.class)
            .filter(objects -> objects.options().contains(getOption()))
            .isPresent();
    }

    protected abstract GroupableByType.Option getOption();
}
