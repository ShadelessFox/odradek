package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.EnumFactEntry;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;

public class EnumFactEntryRenderer implements Renderer.OfObject<EnumFactEntry, ForbiddenWestGame> {
    @Override
    public Optional<String> text(TypeInfo info, EnumFactEntry object, ForbiddenWestGame game) {
        return Optional.of(object.general().name());
    }
}
