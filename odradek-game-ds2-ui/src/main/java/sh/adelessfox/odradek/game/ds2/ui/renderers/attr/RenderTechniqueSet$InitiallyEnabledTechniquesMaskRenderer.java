package sh.adelessfox.odradek.game.ds2.ui.renderers.attr;

import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.rtti.ClassAttrInfo;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;

import java.util.Optional;

public final class RenderTechniqueSet$InitiallyEnabledTechniquesMaskRenderer implements Renderer.OfAttribute<DS2.RenderTechniqueSet, DS2Game> {
    @Override
    public Optional<StyledText> styledText(TypeInfo info, DS2.RenderTechniqueSet object, DS2Game game) {
        var mask = object.general().initiallyEnabledTechniquesMask();
        var builder = StyledText.builder(", ", "[", "]");
        for (int i = 0; i < 64; i++) {
            if ((mask & (1L << i)) != 0) {
                builder.add(String.valueOf(i), StyledFragment.NUMBER);
            }
        }
        return builder.build();
    }

    @Override
    public boolean supports(ClassTypeInfo info, ClassAttrInfo attr) {
        return info.name().equals("RenderTechniqueSet") && attr.name().equals("InitiallyEnabledTechniquesMask");
    }
}
