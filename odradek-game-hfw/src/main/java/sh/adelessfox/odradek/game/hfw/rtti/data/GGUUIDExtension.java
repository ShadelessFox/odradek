package sh.adelessfox.odradek.game.hfw.rtti.data;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.GGUUID;

import java.util.HexFormat;

public interface GGUUIDExtension {
    default String toDisplayString() {
        var object = (GGUUID) this;
        var builder = new StringBuilder(36);
        var format = HexFormat.of();

        format.toHexDigits(builder, object.data0());
        format.toHexDigits(builder, object.data1());
        format.toHexDigits(builder, object.data2());
        format.toHexDigits(builder, object.data3()).append('-');
        format.toHexDigits(builder, object.data4());
        format.toHexDigits(builder, object.data5()).append('-');
        format.toHexDigits(builder, object.data6());
        format.toHexDigits(builder, object.data7()).append('-');
        format.toHexDigits(builder, object.data8());
        format.toHexDigits(builder, object.data9()).append('-');
        format.toHexDigits(builder, object.data10());
        format.toHexDigits(builder, object.data11());
        format.toHexDigits(builder, object.data12());
        format.toHexDigits(builder, object.data13());
        format.toHexDigits(builder, object.data14());
        format.toHexDigits(builder, object.data15());

        return builder.toString();
    }
}
