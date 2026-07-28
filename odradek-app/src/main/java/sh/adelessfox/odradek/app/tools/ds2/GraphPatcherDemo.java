package sh.adelessfox.odradek.app.tools.ds2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class GraphPatcherDemo extends Tool {
    private static final Logger log = LoggerFactory.getLogger(GraphPatcherDemo.class);

    static void main() {
        launch(GraphPatcherDemo.class);
    }

    @Override
    protected void beforeGameLoaded(Path source) throws Throwable {
        var activeGraphPath = source.resolve("LocalCacheWinGame/package/streaming_graph.core");
        var backupGraphPath = source.resolve("LocalCacheWinGame/package/streaming_graph.core.bak");
        if (Files.notExists(backupGraphPath)) {
            log.debug("Backing up streaming_graph.core to {}", backupGraphPath);
            Files.copy(activeGraphPath, backupGraphPath);
        } else {
            log.debug("Restoring streaming_graph.core from backup {}", backupGraphPath);
            Files.copy(backupGraphPath, activeGraphPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    protected void afterGameLoaded(Path source, DS2Game game) throws Throwable {
        var patcher = new GraphPatcher(game);
        patcher.patchObject(
            new ObjectId(56, 40679),
            (DS2.LocalizedTextResource object) -> object
                .text(DS2.ELanguage.English)
                .text("About Odradek"));
        patcher.patchObject(
            new ObjectId(56, 80230),
            (DS2.LocalizedTextResource object) -> object
                .text(DS2.ELanguage.English)
                .text("Details about Odradek can be viewed by selecting \"About Odradek\" from the title screen."));
        patcher.patchObject(
            new ObjectId(56, 33794),
            (DS2.LocalizedTextResource object) -> object
                .text(DS2.ELanguage.English)
                .text("An asset viewer/extractor for Horizon Forbidden West and Death Stranding 2.\n\n© 2025-2026 ShadelessFox and contributors"));
        patcher.persist();
    }
}
