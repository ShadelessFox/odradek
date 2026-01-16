package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.LocalizedTextResource;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;

public final class LocalizedTextResourceRenderer implements Renderer.OfObject<LocalizedTextResource, ForbiddenWestGame> {
    @Override
    public Optional<String> text(TypeInfo info, LocalizedTextResource object, ForbiddenWestGame game) {
        return Optional.of(object.text(game.getWrittenLanguage()));
    }
}
