package sh.adelessfox.odradek.game.ds2.rtti.extensions;

import sh.adelessfox.odradek.game.ds2.rtti.DS2.ELanguage;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.LocalizedTextResource;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.LocalizedTextResourceText;

public interface LocalizedTextResourceExtension {
    default LocalizedTextResourceText text(ELanguage language) {
        var resource = (LocalizedTextResource) this;
        var index = Math.max(0 /* English */, ELanguageExtension.writtenLanguages().indexOf(language));
        return resource.texts().get(index);
    }
}
