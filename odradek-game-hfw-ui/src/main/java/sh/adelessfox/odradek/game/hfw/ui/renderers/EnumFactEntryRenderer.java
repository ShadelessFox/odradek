package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.HFWGame;
import sh.adelessfox.odradek.game.hfw.rtti.HFW.EnumFactEntry;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;

public class EnumFactEntryRenderer implements Renderer.OfObject<EnumFactEntry, HFWGame> {
    @Override
    public Optional<String> text(TypeInfo info, EnumFactEntry object, HFWGame game) {
        return Optional.of(object.general().name());
    }
}
