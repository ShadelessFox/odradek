package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.HFWGame;
import sh.adelessfox.odradek.game.hfw.rtti.HFW.LocalizedTextResource;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;

public final class LocalizedTextResourceRenderer implements Renderer.OfObject<LocalizedTextResource, HFWGame> {
    @Override
    public Optional<String> text(TypeInfo info, LocalizedTextResource object, HFWGame game) {
        return Optional.of(object.text(game.getWrittenLanguage()));
    }
}
