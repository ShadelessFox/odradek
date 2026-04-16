package sh.adelessfox.odradek.game.hfw.rtti.extensions;

import sh.adelessfox.odradek.game.hfw.rtti.HFW.ELanguage;
import sh.adelessfox.odradek.game.hfw.rtti.HFW.LocalizedTextResource;

public interface LocalizedTextResourceExtension {
    default String text(ELanguage language) {
        var resource = (LocalizedTextResource) this;
        var index = Math.max(0 /* English */, ELanguageExtension.writtenLanguages().indexOf(language));
        return resource.texts().get(index);
    }
}
