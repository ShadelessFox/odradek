package sh.adelessfox.odradek.game.hfw.rtti.extensions;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;

public interface EIndexFormatExtension {
    default int stride() {
        var indexFormat = (HFW.EIndexFormat) this;
        return switch (indexFormat) {
            case Index16 -> Short.BYTES;
            case Index32 -> Integer.BYTES;
        };
    }
}
