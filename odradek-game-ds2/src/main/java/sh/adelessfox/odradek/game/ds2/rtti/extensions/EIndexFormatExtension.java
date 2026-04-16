package sh.adelessfox.odradek.game.ds2.rtti.extensions;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;

public interface EIndexFormatExtension {
    default int stride() {
        var indexFormat = (DS2.EIndexFormat) this;
        return switch (indexFormat) {
            case Index16 -> Short.BYTES;
            case Index32 -> Integer.BYTES;
        };
    }
}
