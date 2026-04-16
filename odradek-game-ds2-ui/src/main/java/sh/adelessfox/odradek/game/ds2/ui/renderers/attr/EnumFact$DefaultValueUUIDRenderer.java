package sh.adelessfox.odradek.game.ds2.ui.renderers.attr;

import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.game.ds2.rtti.data.ref.Ref;
import sh.adelessfox.odradek.rtti.ClassAttrInfo;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;

import java.util.Optional;

public final class EnumFact$DefaultValueUUIDRenderer implements Renderer.OfAttribute<DS2.EnumFact, DS2Game> {
    @Override
    public Optional<StyledText> styledText(TypeInfo info, DS2.EnumFact object, DS2Game game) {
        var uuid = object.logic().defaultValueUUID();
        var definition = object.logic().enumDefinition().get();
        for (DS2.EnumFactEntry entry : Ref.unwrap(definition.enumValues())) {
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
