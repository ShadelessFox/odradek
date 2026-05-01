package sh.adelessfox.odradek.game.decima;

import sh.adelessfox.odradek.rtti.ClassTypeInfo;

public interface ObjectIdHolder {
    ObjectId objectId();

    default ClassTypeInfo objectType(DecimaGame game) {
        var id = objectId();
        return game.streamingGraph().group(id.groupId()).types().get(id.objectIndex());
    }
}
