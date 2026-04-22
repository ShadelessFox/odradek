package sh.adelessfox.odradek.app.ui.component.browser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.util.GameLocator;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.ui.components.tree.TreeStructure;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

sealed interface BrowserTreeStructure extends TreeStructure<BrowserTreeStructure> {
    Logger log = LoggerFactory.getLogger(BrowserTreeStructure.class);

    record Root() implements BrowserTreeStructure {
    }

    record Location(Path path) implements BrowserTreeStructure {
    }

    @Override
    default List<? extends BrowserTreeStructure> getChildren() {
        return switch (this) {
            case Root _ -> GameLocator.locators()
                .flatMap(BrowserTreeStructure::findSupportedUnchecked)
                .sorted()
                .distinct()
                .map(Location::new)
                .toList();
            case Location _ -> List.of();
        };
    }

    @Override
    default boolean hasChildren() {
        return switch (this) {
            case Root _ -> true;
            case Location _ -> false;
        };
    }

    private static Stream<Path> findSupportedUnchecked(GameLocator locator) {
        try {
            return locator.findAll().stream()
                .filter(BrowserTreeStructure::isGameSupported);
        } catch (IOException e) {
            log.error("Failed to find games for locator {}", locator.name(), e);
            return Stream.empty();
        }
    }

    private static boolean isGameSupported(Path path) {
        return Game.providers().anyMatch(provider -> provider.supports(path));
    }
}
