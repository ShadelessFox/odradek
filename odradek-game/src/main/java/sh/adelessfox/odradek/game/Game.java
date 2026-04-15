package sh.adelessfox.odradek.game;

import sh.adelessfox.odradek.game.decima.StreamingGraph;
import sh.adelessfox.odradek.rtti.data.TypedObject;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public interface Game extends Closeable {
    interface Provider {
        /**
         * Loads the game from the specified path.
         *
         * @param path path to the game root
         * @return loaded game instance
         * @throws IOException if an I/O error occurs during loading
         */
        Game load(Path path) throws IOException;

        /**
         * Checks whether the provider supports the given path as a game root.
         *
         * @param path path to check
         * @return {@code true} if the provider supports the path, {@code false} otherwise
         */
        boolean supports(Path path);
    }

    static Stream<Provider> providers() {
        class Holder {
            static final List<Provider> providers = ServiceLoader.load(Provider.class).stream()
                .map(ServiceLoader.Provider::get)
                .toList();
        }
        return Holder.providers.stream();
    }

    static Game load(Path path) throws IOException {
        var providers = providers()
            .filter(provider -> provider.supports(path))
            .toList();
        return switch (providers.size()) {
            case 1 -> providers.getFirst().load(path);
            case 0 -> throw new IllegalArgumentException("No provider found for " + path);
            default -> throw new IllegalStateException("Multiple providers found for " + path + ": " + providers);
        };
    }

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

    default StreamingGraph streamingGraph() {
        throw new UnsupportedOperationException("Streaming graph is not supported by this game");
    }
}
