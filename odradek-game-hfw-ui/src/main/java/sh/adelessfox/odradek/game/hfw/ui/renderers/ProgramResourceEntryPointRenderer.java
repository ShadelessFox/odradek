package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.ProgramResourceEntryPoint;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;

public final class ProgramResourceEntryPointRenderer
    implements Renderer.OfObject<ProgramResourceEntryPoint, ForbiddenWestGame> {

    @Override
    public Optional<String> text(TypeInfo info, ProgramResourceEntryPoint object, ForbiddenWestGame game) {
        return Optional.of(object.entryPoint());
    }
}
