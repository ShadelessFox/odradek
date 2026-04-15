package sh.adelessfox.odradek.app.ui.editors;

import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.game.decima.ObjectIdHolder;
import sh.adelessfox.odradek.ui.editors.EditorInput;
import sh.adelessfox.odradek.ui.editors.lazy.LazyEditorInput;

public record ObjectEditorInputLazy(
    ObjectId objectId,
    boolean canLoadImmediately
) implements LazyEditorInput, ObjectIdHolder {
    public ObjectEditorInputLazy(ObjectId objectId) {
        this(objectId, true);
    }

    @Override
    public EditorInput loadRealInput() throws Exception {
        var game = Application.getInstance().game();
        var object = game.readObject(objectId.groupId(), objectId.objectIndex());
        return new ObjectEditorInput(game, object, objectId);
    }

    @Override
    public LazyEditorInput canLoadImmediately(boolean value) {
        return new ObjectEditorInputLazy(objectId, value);
    }

    @Override
    public String getName() {
        return objectId.toString();
    }

    @Override
    public String getDescription() {
        return "Group: %d\nObject: %d".formatted(objectId.groupId(), objectId.objectIndex());
    }

    @Override
    public boolean representsSameInput(EditorInput other) {
        if (other instanceof ObjectEditorInputLazy o) {
            return objectId.equals(o.objectId);
        }
        if (other instanceof ObjectEditorInput o) {
            return objectId.equals(o.objectId());
        }
        return false;
    }
}
