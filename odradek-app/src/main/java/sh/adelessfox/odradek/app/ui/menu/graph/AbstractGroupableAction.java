package sh.adelessfox.odradek.app.ui.menu.graph;

import sh.adelessfox.odradek.app.ui.component.graph.GraphStructure;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.components.tree.StructuredTree;
import sh.adelessfox.odradek.ui.data.DataKeys;

import java.util.Objects;

abstract class AbstractGroupableAction<T extends GraphStructure.Groupable<T, ?>, O extends Enum<O>>
    extends Action
    implements Action.Check {

    @Override
    public void perform(ActionContext context) {
        @SuppressWarnings("unchecked")
        var groupable = (GraphStructure.Groupable<T, O>) context.get(DataKeys.SELECTION, getType()).orElseThrow();
        var tree = context.get(DataKeys.COMPONENT, StructuredTree.class).orElseThrow();

        var option = getOption();
        var options = groupable.options();
        if (!options.remove(option)) {
            options.add(option);
        }

        tree.getModel().refresh(Objects.requireNonNull(tree.getSelectionPath()));
    }

    @Override
    public boolean isVisible(ActionContext context) {
        return context.get(DataKeys.SELECTION, getType()).isPresent();
    }

    @Override
    public boolean isChecked(ActionContext context) {
        return context.get(DataKeys.SELECTION, getType())
            .filter(objects -> objects.options().contains(getOption()))
            .isPresent();
    }

    protected abstract O getOption();

    protected abstract Class<T> getType();
}
