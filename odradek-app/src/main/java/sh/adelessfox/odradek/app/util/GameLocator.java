package sh.adelessfox.odradek.app.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;

/**
 * Locates game installations on the system.
 */
public interface GameLocator {
    static Stream<GameLocator> locators() {
        class Holder {
            static final List<GameLocator> providers = ServiceLoader.load(GameLocator.class).stream()
                .map(ServiceLoader.Provider::get)
                .toList();
        }
        return Holder.providers.stream();
    }

    /**
     * Finds all game installations on the system.
     *
     * @return a list of paths to game installations
     * @throws IOException if an I/O error occurs
     */
    List<Path> findAll() throws IOException;

    String name();
}
