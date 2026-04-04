package sh.adelessfox.odradek.game.ds2.ui.renderers;

import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.LocalizedTextResourceText;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;

import java.util.Optional;

public final class LocalizedTextResourceTextRenderer implements Renderer.OfObject<LocalizedTextResourceText, DS2Game> {
    @Override
    public Optional<StyledText> styledText(TypeInfo info, LocalizedTextResourceText object, DS2Game game) {
        var builder = StyledText.builder()
            .add('"' + object.text() + '"', StyledFragment.STRING);
        return switch (object.mode().unwrap()) {
            case Default -> builder.build();
            case ForcedOn -> builder.add(" (forced on)", StyledFragment.GRAYED).build();
            case ForcedOff -> builder.add(" (forced off)", StyledFragment.GRAYED).build();
        };
    }
}
