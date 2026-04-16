package sh.adelessfox.odradek.game.hfw.ui.renderers.attr;

import sh.adelessfox.odradek.game.hfw.game.HFWGame;
import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.rtti.ClassAttrInfo;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;
import java.util.StringJoiner;

public class RenderTechniqueSet$AvailableTechniquesMaskRenderer implements Renderer.OfAttribute<HFW.RenderTechniqueSet, HFWGame> {
    @Override
    public Optional<String> text(TypeInfo info, HFW.RenderTechniqueSet object, HFWGame game) {
        var mask = object.general().availableTechniquesMask();
        var buffer = new StringJoiner(", ");
        for (int i = 0; i < 32; i++) {
            if ((mask & (1 << i)) != 0) {
                buffer.add(HFW.ERenderTechniqueType.valueOf(i).name());
            }
        }
        return Optional.of(buffer.toString());
    }

    @Override
    public boolean supports(ClassTypeInfo info, ClassAttrInfo attr) {
        return info.name().equals("RenderTechniqueSet") && attr.name().equals("AvailableTechniquesMask");
    }
}
