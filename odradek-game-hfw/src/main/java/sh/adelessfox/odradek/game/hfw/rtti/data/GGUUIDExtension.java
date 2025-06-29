package sh.adelessfox.odradek.game.hfw.rtti.data;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.GGUUID;

import java.util.HexFormat;

public interface GGUUIDExtension {
    default String toDisplayString() {
        var object = (GGUUID) this;
        var builder = new StringBuilder(36);
        var format = HexFormat.of();

        format.toHexDigits(builder, object.data3());
        format.toHexDigits(builder, object.data2());
        format.toHexDigits(builder, object.data1());
        format.toHexDigits(builder, object.data0());
        builder.append('-');
        format.toHexDigits(builder, object.data5());
        format.toHexDigits(builder, object.data4());
        builder.append('-');
        format.toHexDigits(builder, object.data7());
        format.toHexDigits(builder, object.data6());
        builder.append('-');
        format.toHexDigits(builder, object.data8());
        format.toHexDigits(builder, object.data9());
        builder.append('-');
        format.toHexDigits(builder, object.data10());
        format.toHexDigits(builder, object.data11());
        format.toHexDigits(builder, object.data12());
        format.toHexDigits(builder, object.data13());
        format.toHexDigits(builder, object.data14());
        format.toHexDigits(builder, object.data15());

        return builder.toString();
    }
}
