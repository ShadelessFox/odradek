package sh.adelessfox.odradek.game;

import sh.adelessfox.odradek.rtti.runtime.TypedObject;

import java.io.Closeable;
import java.io.IOException;

public interface Game extends Closeable {
    TypedObject readObject(int groupId, int objectIndex) throws IOException;
}
