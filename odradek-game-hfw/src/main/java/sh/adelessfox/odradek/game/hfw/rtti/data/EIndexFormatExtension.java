package sh.adelessfox.odradek.game.hfw.rtti.data;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.EIndexFormat;

public interface EIndexFormatExtension {
    default int stride() {
        var indexFormat = (EIndexFormat) this;
        return switch (indexFormat) {
            case Index16 -> Short.BYTES;
            case Index32 -> Integer.BYTES;
        };
    }
}
