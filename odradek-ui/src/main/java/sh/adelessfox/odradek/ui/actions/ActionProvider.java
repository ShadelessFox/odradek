package sh.adelessfox.odradek.ui.actions;

import java.util.List;

public interface ActionProvider {
    List<Action> create(ActionContext context);
}
