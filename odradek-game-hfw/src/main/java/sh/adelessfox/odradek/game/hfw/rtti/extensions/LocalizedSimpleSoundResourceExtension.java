package sh.adelessfox.odradek.game.hfw.rtti.extensions;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.ELanguage;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.LocalizedDataSource;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.LocalizedSimpleSoundResource;

public interface LocalizedSimpleSoundResourceExtension {
    default LocalizedDataSource localizedDataSource(ELanguage language) {
        var resource = (LocalizedSimpleSoundResource) this;
        var index = Math.max(0 /* English */, ELanguageExtension.spokenLanguages().indexOf(language));
        return resource.streaming().localizedDataSources().get(index);
    }
}
