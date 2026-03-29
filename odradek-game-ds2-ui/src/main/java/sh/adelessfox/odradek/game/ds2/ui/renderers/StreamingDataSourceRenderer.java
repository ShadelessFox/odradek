package sh.adelessfox.odradek.game.ds2.ui.renderers;

import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.EStreamingDataChannel;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.StreamingDataSource;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;

public final class StreamingDataSourceRenderer implements Renderer.OfObject<StreamingDataSource, DS2Game> {
    @Override
    public Optional<String> text(TypeInfo info, StreamingDataSource object, DS2Game game) {
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
