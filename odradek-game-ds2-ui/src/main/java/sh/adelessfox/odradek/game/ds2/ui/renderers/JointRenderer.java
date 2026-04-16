package sh.adelessfox.odradek.game.ds2.ui.renderers;

import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;

import java.util.Optional;

public final class JointRenderer implements Renderer.OfObject<DS2.Joint, DS2Game> {
    @Override
    public Optional<StyledText> styledText(TypeInfo info, DS2.Joint object, DS2Game game) {
        var builder = StyledText.builder();
        if (object.parentIndex() != -1) {
            builder
                .add(String.valueOf(object.parentIndex()), StyledFragment.NUMBER)
                .add(" -> ", StyledFragment.GRAYED);
        }
        return builder
            .add(object.name(), StyledFragment.NAME)
            .build();
    }
}
