package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.HFWGame;
import sh.adelessfox.odradek.game.hfw.rtti.HFW.MurmurHashValue;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;

public class MurmurHashValueRenderer implements Renderer.OfObject<MurmurHashValue, HFWGame> {
    @Override
    public Optional<String> text(TypeInfo info, MurmurHashValue object, HFWGame game) {
        return Optional.of(object.toDisplayString());
    }
}
