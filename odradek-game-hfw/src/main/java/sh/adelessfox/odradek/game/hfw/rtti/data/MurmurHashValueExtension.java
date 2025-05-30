package sh.adelessfox.odradek.game.hfw.rtti.data;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.MurmurHashValue;

import java.util.HexFormat;

public interface MurmurHashValueExtension {
    default String toDisplayString() {
        var object = (MurmurHashValue) this;
        var builder = new StringBuilder(32);
        var format = HexFormat.of();

        format.toHexDigits(builder, object.data0());
        format.toHexDigits(builder, object.data1());
        format.toHexDigits(builder, object.data2());
        format.toHexDigits(builder, object.data4());
        format.toHexDigits(builder, object.data6());
        format.toHexDigits(builder, object.data8());
        format.toHexDigits(builder, object.data10());
        format.toHexDigits(builder, object.data11());
        format.toHexDigits(builder, object.data12());
        format.toHexDigits(builder, object.data13());
        format.toHexDigits(builder, object.data14());
        format.toHexDigits(builder, object.data15());

        return builder.toString();
    }
}
