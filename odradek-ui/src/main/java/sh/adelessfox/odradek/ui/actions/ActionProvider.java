package sh.adelessfox.odradek.ui.actions;

import sh.adelessfox.odradek.data.DataContext;

import java.util.List;

public interface ActionProvider {
    List<? extends Action> create(DataContext context);

    /**
     * Whether the submenu items should be numbered with mnemonics.
     */
    default boolean isList() {
        return false;
    }
}
