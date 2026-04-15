package sh.adelessfox.odradek.game;

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
}
