package sh.adelessfox.odradek.game.hfw.ui.renderers.attr;

import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.EnumFact;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.EnumFactEntry;
import sh.adelessfox.odradek.rtti.ClassAttrInfo;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.rtti.data.Ref;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;

import java.util.Optional;

public class EnumFact$DefaultValueUUIDRenderer implements Renderer.OfAttribute<EnumFact, ForbiddenWestGame> {
    @Override
    public Optional<StyledText> styledText(TypeInfo info, EnumFact object, ForbiddenWestGame game) {
        var uuid = object.logic().defaultValueUUID().toDisplayString();
        var definition = object.logic().enumDefinition().get();
        for (EnumFactEntry entry : Ref.unwrap(definition.enumValues())) {
            // FIXME: Implement proper equals and hashCode on generated POJOs
            if (entry.general().objectUUID().toDisplayString().equals(uuid)) {
                return Optional.of(StyledText.builder()
                    .add(entry.general().name())
                    .add(" (" + uuid + ")", StyledFragment.GRAYED)
                    .build());
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(ClassTypeInfo info, ClassAttrInfo attr) {
        return info.name().equals("EnumFact") && attr.name().equals("DefaultValueUUID");
    }
}
