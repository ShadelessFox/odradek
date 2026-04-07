package sh.adelessfox.odradek.game.ds2.rtti.extensions;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.ELanguage;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.WwiseWemLocalizedResource;

public interface WwiseWemLocalizedResourceExtension {
    default DS2.WwiseWemLocalizedDataSource localizedDataSource(ELanguage language) {
        var resource = (WwiseWemLocalizedResource) this;
        var index = Math.max(0 /* English */, ELanguageExtension.spokenLanguages().indexOf(language));
        return resource.streaming().localizedDataSources().get(index);
    }
}
