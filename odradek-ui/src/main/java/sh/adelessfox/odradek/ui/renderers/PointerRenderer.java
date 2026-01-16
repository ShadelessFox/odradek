package sh.adelessfox.odradek.ui.renderers;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.rtti.data.Ref;
import sh.adelessfox.odradek.rtti.runtime.TypedObject;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.components.StyledText;

import java.util.Optional;

public class PointerRenderer implements Renderer.OfObject<Ref<?>, Game> {
    @Override
    public Optional<StyledText> styledText(TypeInfo info, Ref<?> object, Game game) {
        if (object != null && object.get() instanceof TypedObject to) {
            return Renderer.renderer(to.getType())
                .flatMap(x -> x.styledText(to.getType(), to, game));
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> text(TypeInfo info, Ref<?> object, Game game) {
        if (object == null) {
            return Optional.of("null");
        }
        if (object.get() instanceof TypedObject to) {
            return Renderer.renderer(to.getType())
                .flatMap(x -> x.text(to.getType(), to, game))
                .or(() -> Optional.of("<%s>".formatted(to.getType().name())));
        }
        return Optional.of(object.toString());
    }
}
