package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.GGUUID;
import sh.adelessfox.odradek.rtti.runtime.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;

public class GGUUIDRenderer implements Renderer<GGUUID, ForbiddenWestGame> {
    @Override
    public Optional<String> text(TypeInfo info, GGUUID object, ForbiddenWestGame game) {
        return Optional.of(object.toDisplayString());
    }
}
