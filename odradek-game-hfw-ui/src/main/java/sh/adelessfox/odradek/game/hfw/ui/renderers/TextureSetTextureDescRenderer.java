package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.HFWGame;
import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;

public class TextureSetTextureDescRenderer implements Renderer.OfObject<HFW.TextureSetTextureDesc, HFWGame> {
    @Override
    public Optional<String> text(TypeInfo info, HFW.TextureSetTextureDesc object, HFWGame game) {
        if (object.path().isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(object.path());
        }
    }
}
