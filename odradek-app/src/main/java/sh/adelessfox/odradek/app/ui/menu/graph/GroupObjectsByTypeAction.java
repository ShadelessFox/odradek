package sh.adelessfox.odradek.app.ui.menu.graph;

import sh.adelessfox.odradek.app.ui.component.graph.GraphStructure.GroupableByType;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;

@ActionRegistration(text = "Group objects by type")
@ActionContribution(parent = GraphMenu.ID)
public class GroupObjectsByTypeAction extends AbstractObjectsOptionAction {
    @Override
    public boolean isVisible(ActionContext context) {
        return context.has(DataKeys.SELECTION, GroupableByType.class);
    }

    @Override
    protected GroupableByType.Option getOption() {
        return GroupableByType.Option.GROUP_BY_TYPE;
    }
}
