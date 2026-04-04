package sh.adelessfox.odradek.game.ds2.ui.renderers;

import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.LocalizedTextResource;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;

public final class LocalizedTextResourceRenderer implements Renderer.OfObject<LocalizedTextResource, DS2Game> {
    @Override
    public Optional<String> text(TypeInfo info, LocalizedTextResource object, DS2Game game) {
        return Optional.of(object.text(game.getWrittenLanguage()).text());
    }
}
