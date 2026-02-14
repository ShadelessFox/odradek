package sh.adelessfox.odradek.game.untildawn.rtti.extensions;

import sh.adelessfox.odradek.game.untildawn.rtti.UntilDawn.EIndexFormat;

public interface EIndexFormatExtension {
    default int stride() {
        var indexFormat = (EIndexFormat) this;
        return switch (indexFormat) {
            case Index8 -> Byte.BYTES;
            case Index16 -> Short.BYTES;
            case Index32 -> Integer.BYTES;
        };
    }
}
