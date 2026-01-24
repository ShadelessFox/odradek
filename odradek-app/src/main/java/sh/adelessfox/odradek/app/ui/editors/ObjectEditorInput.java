package sh.adelessfox.odradek.app.ui.editors;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.rtti.runtime.TypedObject;
import sh.adelessfox.odradek.ui.editors.EditorInput;

public record ObjectEditorInput(Game game, TypedObject object, int groupId, int objectIndex) implements EditorInput {
    @Override
    public String getName() {
        return "[%d:%d] %s".formatted(groupId, objectIndex, object.getType());
    }

    @Override
    public String getDescription() {
        return "Group: %d\nObject: %d".formatted(groupId, objectIndex);
    }

    @Override
    public boolean representsSameInput(EditorInput other) {
        if (other instanceof ObjectEditorInput o) {
            return this.groupId == o.groupId && this.objectIndex == o.objectIndex;
        }
        if (other instanceof ObjectEditorInputLazy o) {
            return this.groupId == o.groupId() && this.objectIndex == o.objectIndex();
        }
        return false;
    }
}
