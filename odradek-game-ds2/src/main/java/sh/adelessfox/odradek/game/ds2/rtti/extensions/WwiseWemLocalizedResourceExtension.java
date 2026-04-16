package sh.adelessfox.odradek.game.ds2.rtti.extensions;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;

public interface WwiseWemLocalizedResourceExtension {
    default DS2.WwiseWemLocalizedDataSource localizedDataSource(DS2.ELanguage language) {
        var resource = (DS2.WwiseWemLocalizedResource) this;
        var index = Math.max(0 /* English */, ELanguageExtension.spokenLanguages().indexOf(language));
        return resource.streaming().localizedDataSources().get(index);
    }
}
