package sh.adelessfox.odradek.game.hfw.ui.renderers.attr;

import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.TextureSetEntry;
import sh.adelessfox.odradek.game.hfw.rtti.data.TextureSetPacking;
import sh.adelessfox.odradek.game.hfw.rtti.data.TextureSetPackingChannel;
import sh.adelessfox.odradek.rtti.ClassAttrInfo;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;

import java.util.Optional;

public class TextureSetEntry$PackingInfoRenderer implements Renderer.OfAttribute<TextureSetEntry, ForbiddenWestGame> {
    @Override
    public Optional<String> text(TypeInfo info, TextureSetEntry object, ForbiddenWestGame game) {
        return Optional.of(String.valueOf(object.packingInfo()));
    }

    @Override
    public Optional<StyledText> styledText(TypeInfo info, TextureSetEntry object, ForbiddenWestGame game) {
        var packingInfo = TextureSetPacking.of(object.packingInfo());
        var builder = StyledText.builder();
        appendChannel(packingInfo.red(), "R", builder);
        appendChannel(packingInfo.green(), "G", builder);
        appendChannel(packingInfo.blue(), "B", builder);
        appendChannel(packingInfo.alpha(), "A", builder);
        return builder.build();
    }

    @Override
    public boolean supports(ClassTypeInfo info, ClassAttrInfo attr) {
        return info.name().equals("TextureSetEntry") && attr.name().equals("PackingInfo");
    }

    private static void appendChannel(Optional<TextureSetPackingChannel> channel, String name, StyledText.Builder builder) {
        channel.ifPresent(ch -> {
            if (!builder.isEmpty()) {
                builder.add(", ");
            }
            builder.add(name, StyledFragment.NAME).add("=").add(ch.toString());
        });
    }
}
