package sh.adelessfox.odradek.ui.actions;

import java.util.List;

public interface ActionProvider {
    List<Action> create(ActionContext context);

    /**
     * Whether the submenu items should be numbered with mnemonics.
     */
    default boolean isList() {
        return false;
    }
}
