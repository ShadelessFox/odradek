package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.HFWGame;
import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;

public final class RTTIHandleRenderer implements Renderer.OfObject<HFW.RTTIHandle, HFWGame> {
    @Override
    public Optional<String> text(TypeInfo info, HFW.RTTIHandle object, HFWGame game) {
        return Optional.of(object.staticTypeName());
    }
}
