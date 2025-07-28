package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.StreamingDataSource;
import sh.adelessfox.odradek.rtti.runtime.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;

public class StreamingDataSourceRenderer implements Renderer<StreamingDataSource, ForbiddenWestGame> {
    @Override
    public Optional<String> text(TypeInfo info, StreamingDataSource object, ForbiddenWestGame game) {
        var graph = game.getStreamingGraph();
        var text = "%s (%d bytes)".formatted(
            graph.files().get(object.fileId()),
            object.length()
        );
        return Optional.of(text);
    }
}
