package sh.adelessfox.odradek.game.hfw.ui.renderers.attr;

import sh.adelessfox.odradek.game.hfw.game.HFWGame;
import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.game.hfw.rtti.data.ref.Ref;
import sh.adelessfox.odradek.rtti.ClassAttrInfo;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;

import java.util.Optional;

public class EnumFact$DefaultValueUUIDRenderer implements Renderer.OfAttribute<HFW.EnumFact, HFWGame> {
    @Override
    public Optional<StyledText> styledText(TypeInfo info, HFW.EnumFact object, HFWGame game) {
        var uuid = object.logic().defaultValueUUID();
        var definition = object.logic().enumDefinition().get();
        for (HFW.EnumFactEntry entry : Ref.unwrap(definition.enumValues())) {
            if (entry.general().objectUUID().equals(uuid)) {
                return StyledText.builder()
                    .add(entry.general().name())
                    .add(" (" + uuid.toDisplayString() + ")", StyledFragment.GRAYED)
                    .build();
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(ClassTypeInfo info, ClassAttrInfo attr) {
        return info.name().equals("EnumFact") && attr.name().equals("DefaultValueUUID");
    }
}
