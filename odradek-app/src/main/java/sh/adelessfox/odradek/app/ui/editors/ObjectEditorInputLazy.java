package sh.adelessfox.odradek.app.ui.editors;

import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.ui.editors.EditorInput;
import sh.adelessfox.odradek.ui.editors.lazy.LazyEditorInput;

public record ObjectEditorInputLazy(
    int groupId,
    int objectIndex,
    boolean canLoadImmediately
) implements LazyEditorInput {
    public ObjectEditorInputLazy(int groupId, int objectIndex) {
        this(groupId, objectIndex, true);
    }

    @Override
    public EditorInput loadRealInput() throws Exception {
        var game = Application.getInstance().game();
        var object = game.readObject(groupId, objectIndex);
        return new ObjectEditorInput(game, object, groupId, objectIndex);
    }

    @Override
    public LazyEditorInput canLoadImmediately(boolean value) {
        return new ObjectEditorInputLazy(groupId, objectIndex, value);
    }

    @Override
    public String getName() {
        return "%d:%d".formatted(groupId, objectIndex);
    }

    @Override
    public String getDescription() {
        return "Group: %d\nObject: %d".formatted(groupId, objectIndex);
    }

    @Override
    public boolean representsSameInput(EditorInput other) {
        if (other instanceof ObjectEditorInputLazy o) {
            return this.groupId == o.groupId && this.objectIndex == o.objectIndex;
        }
        if (other instanceof ObjectEditorInput o) {
            return this.groupId == o.groupId() && this.objectIndex == o.objectIndex();
        }
        return false;
    }
}
