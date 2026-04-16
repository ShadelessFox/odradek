package sh.adelessfox.odradek.game.hfw.rtti.extensions;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;

public interface LocalizedTextResourceExtension {
    default String text(HFW.ELanguage language) {
        var resource = (HFW.LocalizedTextResource) this;
        var index = Math.max(0 /* English */, ELanguageExtension.writtenLanguages().indexOf(language));
        return resource.texts().get(index);
    }
}
