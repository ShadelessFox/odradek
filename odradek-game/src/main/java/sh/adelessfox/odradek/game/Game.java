package sh.adelessfox.odradek.game;

import sh.adelessfox.odradek.rtti.runtime.TypedObject;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;

public interface Game extends Closeable {
    /**
     * Reads an object from the streaming graph by its group ID and object index.
     *
     * @param groupId     id of the group that contains the object
     * @param objectIndex index of the object within the group
     * @return the typed object
     * @throws IOException if an I/O error occurs during reading
     */
    TypedObject readObject(int groupId, int objectIndex) throws IOException;

    /**
     * Resolve game-specific path to actual filesystem path.
     * The path should be in a form of {@code <device>:<path>}.
     * <p>
     * Supported devices:
     * <ul>
     *     <li>{@code source} - points to the game root directory</li>
     *     <li>{@code cache} - alias for {@code source:LocalCache<platform>}</li>
     *     <li>{@code tools} - alias for {@code source:tools}</li>
     * </ul>
     *
     * @param path device-specific path
     * @return resolved filesystem path
     */
    Path resolvePath(String path);
}
