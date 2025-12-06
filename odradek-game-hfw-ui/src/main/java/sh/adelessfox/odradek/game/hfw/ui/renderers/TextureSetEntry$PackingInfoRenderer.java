package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.data.PackingInfo;
import sh.adelessfox.odradek.game.hfw.rtti.data.PackingInfoChannel;
import sh.adelessfox.odradek.rtti.ClassAttrInfo;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;

import java.util.Optional;

public class TextureSetEntry$PackingInfoRenderer implements Renderer<Integer, ForbiddenWestGame> {
    @Override
    public Optional<String> text(TypeInfo info, Integer object, ForbiddenWestGame game) {
        return Optional.of(String.valueOf(object));
    }

    @Override
    public Optional<StyledText> styledText(TypeInfo info, Integer object, ForbiddenWestGame game) {
        var packingInfo = PackingInfo.of(object);
        var builder = StyledText.builder();
        appendChannel(packingInfo.red(), "R", builder);
        appendChannel(packingInfo.green(), "G", builder);
        appendChannel(packingInfo.blue(), "B", builder);
        appendChannel(packingInfo.alpha(), "A", builder);
        return Optional.of(builder.build());
    }

    @Override
    public boolean supports(ClassTypeInfo parent, ClassAttrInfo attr) {
        return parent.name().equals("TextureSetEntry") && attr.name().equals("PackingInfo");
    }

    private static void appendChannel(Optional<PackingInfoChannel> channel, String name, StyledText.Builder builder) {
        channel.ifPresent(ch -> {
            if (!builder.isEmpty()) {
                builder.add(", ");
            }
            builder.add(name, StyledFragment.NAME).add("=").add(ch.toString());
        });
    }
}
