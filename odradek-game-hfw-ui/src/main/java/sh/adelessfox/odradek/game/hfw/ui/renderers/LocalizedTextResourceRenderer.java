package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.LocalizedTextResource;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;

public class LocalizedTextResourceRenderer implements Renderer<LocalizedTextResource, ForbiddenWestGame> {
    @Override
    public Optional<String> text(TypeInfo info, LocalizedTextResource object, ForbiddenWestGame game) {
        return Optional.of(object.translations().get(game.getWrittenLanguage().value() - 1));
    }
}
