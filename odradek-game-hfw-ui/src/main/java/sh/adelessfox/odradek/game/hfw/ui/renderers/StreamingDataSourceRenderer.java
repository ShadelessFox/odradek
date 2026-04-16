package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.HFWGame;
import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;

public class StreamingDataSourceRenderer implements Renderer.OfObject<HFW.StreamingDataSource, HFWGame> {
    @Override
    public Optional<String> text(TypeInfo info, HFW.StreamingDataSource object, HFWGame game) {
        if (!object.isPresent()) {
            return Optional.of("<empty>");
        }
        var text = "%s (%d bytes, %s)".formatted(
            game.streamingGraph().files().get(object.fileId()),
            object.length(),
            HFW.EStreamingDataChannel.valueOf(object.channel())
        );
        return Optional.of(text);
    }
}
