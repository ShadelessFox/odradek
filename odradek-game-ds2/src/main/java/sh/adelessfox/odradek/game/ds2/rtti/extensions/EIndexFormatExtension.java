package sh.adelessfox.odradek.game.ds2.rtti.extensions;

import sh.adelessfox.odradek.game.ds2.rtti.DS2.EIndexFormat;

public interface EIndexFormatExtension {
    default int stride() {
        var indexFormat = (EIndexFormat) this;
        return switch (indexFormat) {
            case Index16 -> Short.BYTES;
            case Index32 -> Integer.BYTES;
        };
    }
}
