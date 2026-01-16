package sh.adelessfox.odradek.game.hfw.rtti.extensions;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.ELanguage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public interface ELanguageExtension {
    int IS_WRITTEN_LANGUAGE = 1;
    int IS_SPOKEN_LANGUAGE = 2;
    int IS_DEFAULT_LANGUAGE = 4;

    static List<ELanguage> writtenLanguages() {
        class Holder {
            static final List<ELanguage> languages = Stream.of(ELanguage.values())
                .filter(ELanguage::isWrittenLanguage)
                .sorted(Comparator.comparingInt(ELanguage::value))
                .toList();
        }
        return Holder.languages;
    }

    static List<ELanguage> spokenLanguages() {
        class Holder {
            static final List<ELanguage> languages = Stream.of(ELanguage.values())
                .filter(ELanguage::isSpokenLanguage)
                .sorted(Comparator.comparingInt(ELanguage::value))
                .toList();
        }
        return Holder.languages;
    }

    default boolean isWrittenLanguage() {
        return (flags() & IS_WRITTEN_LANGUAGE) != 0;
    }

    default boolean isSpokenLanguage() {
        return (flags() & IS_SPOKEN_LANGUAGE) != 0;
    }

    default boolean isDefaultLanguage() {
        return (flags() & IS_DEFAULT_LANGUAGE) != 0;
    }

    default int flags() {
        return switch ((ELanguage) this) {
            case English -> IS_WRITTEN_LANGUAGE | IS_SPOKEN_LANGUAGE | IS_DEFAULT_LANGUAGE;

            case French,
                 Arabic,
                 LATAMPOR,
                 LATAMSP,
                 Japanese,
                 Polish,
                 Russian,
                 Portuguese,
                 Italian,
                 German,
                 Spanish -> IS_WRITTEN_LANGUAGE | IS_SPOKEN_LANGUAGE;

            case Dutch,
                 Chinese_Uncensored,
                 Greek,
                 Czech,
                 Hungarian,
                 Thai,
                 Chinese_Simplified,
                 Turkish,
                 Swedish,
                 Norwegian,
                 Finnish,
                 Danish,
                 Korean,
                 Chinese_Traditional -> IS_WRITTEN_LANGUAGE;

            case Unknown -> 0;
        };
    }
}
