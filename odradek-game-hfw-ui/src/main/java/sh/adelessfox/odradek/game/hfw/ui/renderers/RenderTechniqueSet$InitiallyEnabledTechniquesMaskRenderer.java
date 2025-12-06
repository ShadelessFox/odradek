package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.rtti.ClassAttrInfo;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;
import java.util.StringJoiner;

public class RenderTechniqueSet$InitiallyEnabledTechniquesMaskRenderer implements Renderer<Long, ForbiddenWestGame> {
    @Override
    public Optional<String> text(TypeInfo info, Long object, ForbiddenWestGame game) {
        var mask = (long) object;
        var buffer = new StringJoiner(", ");
        for (int i = 0; i < 64; i++) {
            if ((mask & (1L << i)) != 0) {
                buffer.add(String.valueOf(i));
            }
        }
        return Optional.of(buffer.toString());
    }

    @Override
    public boolean supports(ClassTypeInfo parent, ClassAttrInfo attr) {
        return parent.name().equals("RenderTechniqueSet") && attr.name().equals("InitiallyEnabledTechniquesMask");
    }
}
