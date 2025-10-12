package sh.adelessfox.odradek.app.editors;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.rtti.runtime.TypedObject;
import sh.adelessfox.odradek.ui.editors.EditorInput;

public record ObjectEditorInput(Game game, TypedObject object, int groupId, int objectIndex) implements EditorInput {
    @Override
    public String getName() {
        return object.getType().toString();
    }

    @Override
    public String getDescription() {
        return "Group: %d\nObject: %d".formatted(groupId, objectIndex);
    }

    @Override
    public boolean representsSameInput(EditorInput other) {
        return equals(other);
    }
}
