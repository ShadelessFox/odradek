package sh.adelessfox.odradek.game.ds2.rtti.extensions;

import sh.adelessfox.odradek.game.ds2.rtti.DS2.ELanguage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public interface ELanguageExtension {
    static List<ELanguage> writtenLanguages() {
        class Holder {
            static final List<ELanguage> languages = Stream.of(ELanguage.values())
                .filter(ELanguage::isWrittenLanguage)
                .sorted(Comparator.comparingInt(ELanguage::value))
                .toList();
        }
        return Holder.languages;
    }

    default boolean isWrittenLanguage() {
        return this != ELanguage.Unknown;
    }
}
