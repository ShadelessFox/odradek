package sh.adelessfox.odradek.game.ds2.ui.renderers;

import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;

public final class StreamingDataSourceRenderer implements Renderer.OfObject<DS2.StreamingDataSource, DS2Game> {
    @Override
    public Optional<String> text(TypeInfo info, DS2.StreamingDataSource object, DS2Game game) {
        if (!object.isPresent()) {
            return Optional.of("<empty>");
        }
        var text = "%s (%d bytes, %s)".formatted(
            game.streamingGraph().files().get(object.fileId()),
            object.length(),
            DS2.EStreamingDataChannel.valueOf(object.channel())
        );
        return Optional.of(text);
    }
}
