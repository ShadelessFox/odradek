package sh.adelessfox.odradek.game.ds2.ui.renderers;

import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;

import java.util.Optional;

public final class RenderTechniqueRenderer implements Renderer.OfObject<DS2.RenderTechnique, DS2Game> {
    @Override
    public Optional<StyledText> styledText(TypeInfo info, DS2.RenderTechnique object, DS2Game game) {
        int packedData = object.general().packedData();
        var techniqueType = DS2.ERenderTechniqueType.valueOf((packedData & 0x3f) - 1);
        var initiallyEnabled = (packedData & 0x40) != 0;

        return StyledText.builder()
            .add(techniqueType.name(), StyledFragment.NAME)
            .add(" (")
            .add(initiallyEnabled ? "enabled" : "disabled")
            .add(")")
            .build();
    }
}
