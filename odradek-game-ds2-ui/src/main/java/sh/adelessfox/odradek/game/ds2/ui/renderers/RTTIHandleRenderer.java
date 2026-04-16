package sh.adelessfox.odradek.game.ds2.ui.renderers;

import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;

public final class RTTIHandleRenderer implements Renderer.OfObject<DS2.RTTIHandle, DS2Game> {
    @Override
    public Optional<String> text(TypeInfo info, DS2.RTTIHandle object, DS2Game game) {
        return Optional.of(object.staticTypeName());
    }
}
