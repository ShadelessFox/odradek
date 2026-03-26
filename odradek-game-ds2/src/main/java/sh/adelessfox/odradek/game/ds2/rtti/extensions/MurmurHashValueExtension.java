package sh.adelessfox.odradek.game.ds2.rtti.extensions;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;

import java.util.HexFormat;

public interface MurmurHashValueExtension {
    default String toDisplayString() {
        var object = (DS2.MurmurHashValue) this;
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
