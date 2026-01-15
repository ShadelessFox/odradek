package sh.adelessfox.odradek.ui.renderers;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.rtti.AtomTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;

import java.util.Optional;

public class NumberRenderer implements Renderer.OfObject<Number, Game> {
    @Override
    public Optional<String> text(TypeInfo info, Number object, Game game) {
        var unsigned = isUnsigned(info);
        var value = switch (object) {
            case Byte b when unsigned -> String.valueOf(Byte.toUnsignedInt(b));
            case Short s when unsigned -> String.valueOf(Short.toUnsignedInt(s));
            case Integer i when unsigned -> Integer.toUnsignedString(i);
            case Long l when unsigned -> Long.toUnsignedString(l);
            default -> String.valueOf(object);
        };
        return Optional.of(value);
    }

    @Override
    public Optional<StyledText> styledText(TypeInfo info, Number object, Game game) {
        return text(info, object, game)
            .map(text -> StyledText.builder()
                .add(text, StyledFragment.NUMBER)
                .build());
    }

    @Override
    public boolean supports(TypeInfo info) {
        return info instanceof AtomTypeInfo && isNumber(info);
    }

    private static boolean isUnsigned(TypeInfo info) {
        return info.name().startsWith("uint");
    }

    private static boolean isNumber(TypeInfo info) {
        Class<?> type = info.type();
        return type == byte.class
            || type == short.class
            || type == int.class
            || type == long.class
            || type == float.class
            || type == double.class;
    }
}
