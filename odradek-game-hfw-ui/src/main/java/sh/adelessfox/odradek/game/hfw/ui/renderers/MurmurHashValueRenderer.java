package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.HFWGame;
import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;

public class MurmurHashValueRenderer implements Renderer.OfObject<HFW.MurmurHashValue, HFWGame> {
    @Override
    public Optional<String> text(TypeInfo info, HFW.MurmurHashValue object, HFWGame game) {
        return Optional.of(object.toDisplayString());
    }
}
