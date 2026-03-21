package sh.adelessfox.odradek.ui.renderers;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.game.ObjectHolder;
import sh.adelessfox.odradek.rtti.PointerTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.rtti.data.TypedObject;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.components.StyledText;

import java.util.Optional;

public class PointerRenderer implements Renderer.OfObject<Object, Game> {
    @Override
    public Optional<StyledText> styledText(TypeInfo info, Object object, Game game) {
        if (object instanceof ObjectHolder<?> holder && holder.object() instanceof TypedObject to) {
            return Renderer.renderer(to.getType(), game)
                .flatMap(x -> x.styledText(to.getType(), to, game));
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> text(TypeInfo info, Object object, Game game) {
        if (object == null) {
            return Optional.of("null");
        }
        if (object instanceof ObjectHolder<?> holder && holder.object() instanceof TypedObject to) {
            return Renderer.renderer(to.getType(), game)
                .flatMap(x -> x.text(to.getType(), to, game))
                .or(() -> Optional.of("<%s>".formatted(to.getType().name())));
        }
        return Optional.of(object.toString());
    }

    @Override
    public boolean supports(TypeInfo info) {
        return info instanceof PointerTypeInfo;
    }
}
