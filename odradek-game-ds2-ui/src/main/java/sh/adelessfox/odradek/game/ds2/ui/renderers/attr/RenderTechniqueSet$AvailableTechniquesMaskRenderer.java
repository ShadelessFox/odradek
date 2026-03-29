package sh.adelessfox.odradek.game.ds2.ui.renderers.attr;

import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.ERenderTechniqueType;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.RenderTechniqueSet;
import sh.adelessfox.odradek.rtti.ClassAttrInfo;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;
import java.util.StringJoiner;

public final class RenderTechniqueSet$AvailableTechniquesMaskRenderer implements Renderer.OfAttribute<RenderTechniqueSet, DS2Game> {
    @Override
    public Optional<String> text(TypeInfo info, RenderTechniqueSet object, DS2Game game) {
        var mask = object.general().availableTechniquesMask();
        var buffer = new StringJoiner(", ");
        for (int i = 0; i < 32; i++) {
            if ((mask & (1 << i)) != 0) {
                buffer.add(ERenderTechniqueType.valueOf(i).name());
            }
        }
        return Optional.of(buffer.toString());
    }

    @Override
    public boolean supports(ClassTypeInfo info, ClassAttrInfo attr) {
        return info.name().equals("RenderTechniqueSet") && attr.name().equals("AvailableTechniquesMask");
    }
}
