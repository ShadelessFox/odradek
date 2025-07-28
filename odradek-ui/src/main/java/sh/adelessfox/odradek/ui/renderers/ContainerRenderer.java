package sh.adelessfox.odradek.ui.renderers;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.rtti.runtime.ContainerTypeInfo;
import sh.adelessfox.odradek.rtti.runtime.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;

public class ContainerRenderer implements Renderer<Object, Game> {
    @Override
    public Optional<String> text(TypeInfo info, Object object, Game game) {
        int length = ((ContainerTypeInfo) info).length(object);
        var value = switch (length) {
            case 0 -> "(empty)";
            case 1 -> "(1 item)";
            default -> "(%d items)".formatted(length);
        };
        return Optional.of(value);
    }

    @Override
    public boolean supports(TypeInfo info) {
        return info instanceof ContainerTypeInfo;
    }
}
