package sh.adelessfox.odradek.ui.data;

import sh.adelessfox.odradek.game.Game;

import javax.swing.*;
import java.util.List;

public final class DataKeys {
    private DataKeys() {
    }

    public static final DataKey<JComponent> COMPONENT = DataKey.create("component");
    public static final DataKey<Object> SELECTION = DataKey.create("selection");
    public static final DataKey<List<Object>> SELECTION_LIST = DataKey.create("selection list");
    public static final DataKey<Game> GAME = DataKey.create("game");
}
