package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.SentenceResource;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;

import java.util.Optional;

public final class SentenceResourceRenderer implements Renderer.OfObject<SentenceResource, ForbiddenWestGame> {
    @Override
    public Optional<StyledText> styledText(TypeInfo info, SentenceResource object, ForbiddenWestGame game) {
        var textRef = object.general().text();
        var voiceRef = object.general().voice();
        if (textRef == null || voiceRef == null) {
            return Optional.empty();
        }

        var text = textRef.get().text(game.getWrittenLanguage());
        var voice = voiceRef.get().general().nameResource().get().text(game.getWrittenLanguage());
        return StyledText.builder()
            .add("[" + voice + "] ", StyledFragment.NAME)
            .add(text)
            .build();
    }
}
