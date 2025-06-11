package sh.adelessfox.odradek.app.menu.actions.graph;

import sh.adelessfox.odradek.app.GraphStructure;
import sh.adelessfox.odradek.app.menu.ActionIds;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;

@ActionRegistration(id = "sh.adelessfox.odradek.app.menu.actions.graph.GroupObjectsByTypeAction", name = "Group objects by type")
@ActionContribution(parent = ActionIds.GRAPH_MENU_ID, order = 1)
public class GroupObjectsByTypeAction extends AbstractObjectsOptionAction {

    @Override
    protected GraphStructure.GroupObjects.Options getOption() {
        return GraphStructure.GroupObjects.Options.GROUP_BY_TYPE;
    }
}
