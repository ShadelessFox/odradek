package sh.adelessfox.odradek.game.decima;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.rtti.data.TypedObject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface DecimaGame extends Game {
    /**
     * Reads a group from the streaming graph by its group ID.
     *
     * @param groupId id of the group
     * @return a list of objects in the group
     * @throws IOException if an I/O error occurs during reading
     */
    default List<TypedObject> readGroup(int groupId) throws IOException {
        return readGroup(groupId, true);
    }

    /**
     * Reads a group from the streaming graph by its group ID.
     *
     * @param groupId       id of the group
     * @param readSubgroups whether to read subgroups of the group
     * @return a list of objects in the group
     * @throws IOException if an I/O error occurs during reading
     */
    List<TypedObject> readGroup(int groupId, boolean readSubgroups) throws IOException;

    /**
     * Reads an object from the streaming graph by its group ID and object index.
     *
     * @param groupId     id of the group that contains the object
     * @param objectIndex index of the object within the group
     * @return the typed object
     * @throws IOException if an I/O error occurs during reading
     */
    default TypedObject readObject(int groupId, int objectIndex) throws IOException {
        return readGroup(groupId).get(objectIndex);
    }

    byte[] readFile(String file, long offset, long length) throws IOException;

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

    StreamingGraph streamingGraph();
}
