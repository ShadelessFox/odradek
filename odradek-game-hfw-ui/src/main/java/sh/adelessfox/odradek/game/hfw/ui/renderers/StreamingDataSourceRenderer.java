package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.EStreamingDataChannel;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.StreamingDataSource;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;

public class StreamingDataSourceRenderer implements Renderer<StreamingDataSource, ForbiddenWestGame> {
    @Override
    public Optional<String> text(TypeInfo info, StreamingDataSource object, ForbiddenWestGame game) {
        if (!object.isPresent()) {
            return Optional.of("<empty>");
        }
        var graph = game.getStreamingGraph();
        var text = "%s (%d bytes, %s)".formatted(
            graph.files().get(object.fileId()),
            object.length(),
            EStreamingDataChannel.valueOf(object.channel())
        );
        return Optional.of(text);
    }
}
