package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.rtti.AtomTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;

public class UnsignedIntRenderer implements Renderer<Number, Game> {
    @Override
    public Optional<String> text(TypeInfo info, Number object, Game game) {
        var value = switch (object) {
            case Byte b -> String.valueOf(Byte.toUnsignedInt(b));
            case Short s -> String.valueOf(Short.toUnsignedInt(s));
            case Integer i -> Integer.toUnsignedString(i);
            case Long l -> Long.toUnsignedString(l);
            default -> null;
        };
        return Optional.ofNullable(value);
    }

    @Override
    public boolean supports(TypeInfo info) {
        return info instanceof AtomTypeInfo && info.name().startsWith("uint");
    }
}
