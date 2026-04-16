package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.HFWGame;
import sh.adelessfox.odradek.game.hfw.rtti.HFW.ProgramParameter;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;

import java.util.Optional;

public final class ProgramParameterRenderer implements Renderer.OfObject<ProgramParameter, HFWGame> {
    @Override
    public Optional<StyledText> styledText(TypeInfo info, ProgramParameter object, HFWGame game) {
        return StyledText.builder()
            .add(object.name(), StyledFragment.NAME)
            .add(": " + object.type().staticTypeName(), StyledFragment.GRAYED)
            .build();
    }
}
