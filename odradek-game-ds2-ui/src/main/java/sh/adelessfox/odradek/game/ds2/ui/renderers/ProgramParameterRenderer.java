package sh.adelessfox.odradek.game.ds2.ui.renderers;

import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;

import java.util.Optional;

public final class ProgramParameterRenderer implements Renderer.OfObject<DS2.ProgramParameter, DS2Game> {
    @Override
    public Optional<StyledText> styledText(TypeInfo info, DS2.ProgramParameter object, DS2Game game) {
        return StyledText.builder()
            .add(object.name(), StyledFragment.NAME)
            .add(": " + object.type().staticTypeName(), StyledFragment.GRAYED)
            .build();
    }
}
