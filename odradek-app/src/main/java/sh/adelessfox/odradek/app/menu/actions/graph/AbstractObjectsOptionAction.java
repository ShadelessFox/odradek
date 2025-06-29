package sh.adelessfox.odradek.app.menu.actions.graph;

import sh.adelessfox.odradek.app.GraphStructure.GroupObjects;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.tree.StructuredTree;

import java.util.Objects;

abstract class AbstractObjectsOptionAction extends Action implements Action.Check {
    @Override
    public void perform(ActionContext context) {
        var tree = context.get(DataKeys.COMPONENT, StructuredTree.class).orElseThrow();
        var objects = context.get(DataKeys.SELECTION, GroupObjects.class).orElseThrow();

        var option = getOption();
        var options = objects.options();
        if (!options.remove(option)) {
            options.add(option);
        }

        tree.getModel().update(Objects.requireNonNull(tree.getSelectionPath()));
    }

    @Override
    public boolean isVisible(ActionContext context) {
        return context.get(DataKeys.SELECTION, GroupObjects.class).isPresent();
    }

    @Override
    public boolean isChecked(ActionContext context) {
        return context.get(DataKeys.SELECTION, GroupObjects.class)
            .filter(objects -> objects.options().contains(getOption()))
            .isPresent();
    }

    protected abstract GroupObjects.Option getOption();
}
