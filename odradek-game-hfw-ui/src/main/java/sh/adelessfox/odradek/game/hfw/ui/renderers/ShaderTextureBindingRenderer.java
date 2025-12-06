package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.ShaderTextureBinding;
import sh.adelessfox.odradek.game.hfw.rtti.data.TextureBindingPacking;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;

import java.util.Optional;

public class ShaderTextureBindingRenderer implements Renderer<ShaderTextureBinding, ForbiddenWestGame> {
    @Override
    public Optional<StyledText> styledText(TypeInfo info, ShaderTextureBinding object, ForbiddenWestGame game) {
        var builder = StyledText.builder();

        switch (TextureBindingPacking.of(object.packedData())) {
            case TextureBindingPacking.Texture _ -> builder
                .add("Type", StyledFragment.NAME).add(": ").add("Texture");
            case TextureBindingPacking.TextureSet(var packingInfo) -> builder
                .add("Type", StyledFragment.NAME).add(": ").add("TextureSet, ")
                .add("Target", StyledFragment.NAME).add(": ").add(packingInfo.red().orElseThrow().type().toString());
        }

        return Optional.of(builder.build());
    }

}
