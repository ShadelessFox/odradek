package sh.adelessfox.odradek.app.menu.actions.graph;

import sh.adelessfox.odradek.app.GraphStructure.GroupableByType;
import sh.adelessfox.odradek.app.menu.ActionIds;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;

@ActionRegistration(text = "Group objects by type")
@ActionContribution(parent = ActionIds.GRAPH_MENU_ID, order = 1)
public class GroupObjectsByTypeAction extends AbstractObjectsOptionAction {
    @Override
    public boolean isVisible(ActionContext context) {
        return context.get(DataKeys.SELECTION, GroupableByType.class).stream()
            .flatMap(GroupableByType::types)
            .distinct().limit(2).count() == 2;
    }

    @Override
    protected GroupableByType.Option getOption() {
        return GroupableByType.Option.GROUP_BY_TYPE;
    }
}
