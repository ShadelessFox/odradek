package sh.adelessfox.odradek.game.ds2.rtti.extensions;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;

public interface LocalizedTextResourceExtension {
    default DS2.LocalizedTextResourceText text(DS2.ELanguage language) {
        var resource = (DS2.LocalizedTextResource) this;
        var index = Math.max(0 /* English */, ELanguageExtension.writtenLanguages().indexOf(language));
        return resource.texts().get(index);
    }
}
