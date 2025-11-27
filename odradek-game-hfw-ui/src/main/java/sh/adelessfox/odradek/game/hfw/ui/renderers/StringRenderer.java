package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;

import java.util.Optional;

public class StringRenderer implements Renderer<String, Game> {
    @Override
    public Optional<String> text(TypeInfo info, String object, Game game) {
        return Optional.ofNullable(object);
    }

    @Override
    public Optional<StyledText> styledText(TypeInfo info, String object, Game game) {
        return text(info, object, game)
            .map(text -> StyledText.builder()
                .add('"' + text + '"', StyledFragment.STRING)
                .build());
    }
}
