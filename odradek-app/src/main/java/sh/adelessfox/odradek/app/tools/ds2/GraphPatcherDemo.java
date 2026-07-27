package sh.adelessfox.odradek.app.tools.ds2;

import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;

import java.nio.file.Path;

public final class GraphPatcherDemo extends Tool {
    static void main() {
        launch(GraphPatcherDemo.class);
    }

    @Override
    protected void run(Path source, DS2Game game) throws Throwable {
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
