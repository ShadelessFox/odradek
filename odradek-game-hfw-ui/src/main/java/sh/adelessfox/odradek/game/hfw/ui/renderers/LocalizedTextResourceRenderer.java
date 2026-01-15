package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.ELanguage;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.LocalizedTextResource;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class LocalizedTextResourceRenderer implements Renderer.OfObject<LocalizedTextResource, ForbiddenWestGame> {
    private static final List<ELanguage> writtenLanguages = Stream.of(ELanguage.values())
        .filter(ELanguage::isWrittenLanguage)
        .sorted(Comparator.comparingInt(ELanguage::value))
        .toList();

    @Override
    public Optional<String> text(TypeInfo info, LocalizedTextResource object, ForbiddenWestGame game) {
        int index = Math.max(0, writtenLanguages.indexOf(game.getWrittenLanguage()));
        return Optional.of(object.translations().get(index));
    }
}
