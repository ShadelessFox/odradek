package sh.adelessfox.odradek.app.menu.actions.graph;

import sh.adelessfox.odradek.Gatherers;
import sh.adelessfox.odradek.app.GraphStructure;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.tree.StructuredTree;

import java.util.Objects;

abstract class AbstractObjectsOptionAction extends Action implements Action.Check {
    @Override
    public void perform(ActionContext context) {
        var tree = (StructuredTree<?>) context.getData(DataKeys.COMPONENT).orElseThrow();
        var objects = (GraphStructure.GroupObjects) context.getData(DataKeys.SELECTION).orElseThrow();

        var option = getOption();
        var options = objects.options();
        if (!options.remove(option)) {
            options.add(option);
        }

        tree.getModel().update(Objects.requireNonNull(tree.getSelectionPath()));
    }

    @Override
    public boolean isVisible(ActionContext context) {
        return context.getData(DataKeys.SELECTION)
            .filter(GraphStructure.GroupObjects.class::isInstance)
            .isPresent();
    }

    @Override
    public boolean isChecked(ActionContext context) {
        return context.getData(DataKeys.SELECTION).stream()
            .gather(Gatherers.instanceOf(GraphStructure.GroupObjects.class))
            .anyMatch(objects -> objects.options().contains(getOption()));
    }

    protected abstract GraphStructure.GroupObjects.Option getOption();
}
