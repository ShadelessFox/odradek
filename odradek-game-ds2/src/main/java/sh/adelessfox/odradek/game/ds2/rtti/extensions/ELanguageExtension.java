package sh.adelessfox.odradek.game.ds2.rtti.extensions;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public interface ELanguageExtension {
    static List<DS2.ELanguage> writtenLanguages() {
        class Holder {
            static final List<DS2.ELanguage> languages = Stream.of(DS2.ELanguage.values())
                .filter(DS2.ELanguage::isWrittenLanguage)
                .sorted(Comparator.comparingInt(DS2.ELanguage::value))
                .toList();
        }
        return Holder.languages;
    }

    static List<DS2.ELanguage> spokenLanguages() {
        class Holder {
            static final List<DS2.ELanguage> languages = Stream.of(DS2.ELanguage.values())
                .filter(DS2.ELanguage::isSpokenLanguage)
                .sorted(Comparator.comparingInt(DS2.ELanguage::value))
                .toList();
        }
        return Holder.languages;
    }

    default boolean isWrittenLanguage() {
        return this != DS2.ELanguage.Unknown;
    }

    default boolean isSpokenLanguage() {
        return this == DS2.ELanguage.English
            || this == DS2.ELanguage.French
            || this == DS2.ELanguage.Spanish
            || this == DS2.ELanguage.German
            || this == DS2.ELanguage.Italian
            || this == DS2.ELanguage.Portuguese
            || this == DS2.ELanguage.Russian
            || this == DS2.ELanguage.Polish
            || this == DS2.ELanguage.Japanese
            || this == DS2.ELanguage.LATAMSP
            || this == DS2.ELanguage.LATAMPOR
            || this == DS2.ELanguage.Chinese_Simplified;
    }
}
