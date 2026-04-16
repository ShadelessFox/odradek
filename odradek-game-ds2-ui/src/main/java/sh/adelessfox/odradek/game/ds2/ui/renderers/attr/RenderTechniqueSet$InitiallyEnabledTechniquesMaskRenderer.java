package sh.adelessfox.odradek.game.ds2.ui.renderers.attr;

import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.rtti.ClassAttrInfo;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;
import java.util.StringJoiner;

public final class RenderTechniqueSet$InitiallyEnabledTechniquesMaskRenderer implements Renderer.OfAttribute<DS2.RenderTechniqueSet, DS2Game> {
    @Override
    public Optional<String> text(TypeInfo info, DS2.RenderTechniqueSet object, DS2Game game) {
        var mask = object.general().initiallyEnabledTechniquesMask();
        var buffer = new StringJoiner(", ");
        for (int i = 0; i < 64; i++) {
            if ((mask & (1L << i)) != 0) {
                buffer.add(String.valueOf(i));
            }
        }
        return Optional.of(buffer.toString());
    }

    @Override
    public boolean supports(ClassTypeInfo info, ClassAttrInfo attr) {
        return info.name().equals("RenderTechniqueSet") && attr.name().equals("InitiallyEnabledTechniquesMask");
    }
}
