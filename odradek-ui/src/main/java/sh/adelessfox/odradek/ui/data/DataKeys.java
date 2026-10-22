package sh.adelessfox.odradek.ui.data;

import sh.adelessfox.odradek.data.DataKey;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.ui.editors.Editor;
import sh.adelessfox.odradek.ui.editors.EditorManager;
import sh.adelessfox.odradek.ui.editors.stack.EditorStack;

import javax.swing.*;
import java.util.List;

public final class DataKeys {
    public static final DataKey<JComponent> COMPONENT = DataKey.of(JComponent.class).named("component");
    public static final DataKey<Object> SELECTION = DataKey.of(Object.class).named("selection");
    public static final DataKey<List<Object>> SELECTION_LIST = DataKey.listOf(Object.class).named("selection");
    public static final DataKey<Game> GAME = DataKey.of(Game.class);
    public static final DataKey<EventBus> EVENT_BUS = DataKey.of(EventBus.class);

    public static final DataKey<EditorManager> EDITOR_MANAGER = DataKey.of(EditorManager.class);
    public static final DataKey<EditorStack> EDITOR_STACK = DataKey.of(EditorStack.class);
    public static final DataKey<Editor> EDITOR = DataKey.of(Editor.class);

    private DataKeys() {
    }
}
