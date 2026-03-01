package sh.adelessfox.odradek.app.ui.editors;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.game.ObjectId;
import sh.adelessfox.odradek.game.ObjectIdHolder;
import sh.adelessfox.odradek.rtti.data.TypedObject;
import sh.adelessfox.odradek.ui.editors.EditorInput;

public record ObjectEditorInput(
    Game game,
    TypedObject object,
    ObjectId objectId
) implements EditorInput, ObjectIdHolder {
    @Override
    public String getName() {
        return "[%s] %s".formatted(objectId, object.getType());
    }

    @Override
    public String getDescription() {
        return "Group: %d\nObject: %d".formatted(objectId.groupId(), objectId.objectIndex());
    }

    @Override
    public boolean representsSameInput(EditorInput other) {
        if (other instanceof ObjectEditorInput o) {
            return objectId.equals(o.objectId);
        }
        if (other instanceof ObjectEditorInputLazy o) {
            return objectId.equals(o.objectId());
        }
        return false;
    }
}
