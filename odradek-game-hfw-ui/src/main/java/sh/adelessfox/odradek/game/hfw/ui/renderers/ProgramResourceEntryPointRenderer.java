package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.HFWGame;
import sh.adelessfox.odradek.game.hfw.rtti.HFW.ProgramResourceEntryPoint;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;

public final class ProgramResourceEntryPointRenderer
    implements Renderer.OfObject<ProgramResourceEntryPoint, HFWGame> {

    @Override
    public Optional<String> text(TypeInfo info, ProgramResourceEntryPoint object, HFWGame game) {
        return Optional.of(object.entryPoint());
    }
}
