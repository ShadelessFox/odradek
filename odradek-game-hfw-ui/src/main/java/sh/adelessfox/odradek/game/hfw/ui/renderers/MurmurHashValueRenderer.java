package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.MurmurHashValue;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;

public class MurmurHashValueRenderer implements Renderer<MurmurHashValue, ForbiddenWestGame> {
    @Override
    public Optional<String> text(TypeInfo info, MurmurHashValue object, ForbiddenWestGame game) {
        return Optional.of(object.toDisplayString());
    }
}
