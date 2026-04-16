package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.HFWGame;
import sh.adelessfox.odradek.game.hfw.rtti.HFW.Joint;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;

import java.util.Optional;

public final class JointRenderer implements Renderer.OfObject<Joint, HFWGame> {
    @Override
    public Optional<StyledText> styledText(TypeInfo info, Joint object, HFWGame game) {
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
