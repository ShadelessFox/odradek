package sh.adelessfox.odradek.game.hfw.rtti.extensions;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;

public interface LocalizedSimpleSoundResourceExtension {
    default HFW.LocalizedDataSource localizedDataSource(HFW.ELanguage language) {
        var resource = (HFW.LocalizedSimpleSoundResource) this;
        var index = Math.max(0 /* English */, ELanguageExtension.spokenLanguages().indexOf(language));
        return resource.streaming().localizedDataSources().get(index);
    }
}
