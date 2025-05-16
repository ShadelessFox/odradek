package sh.adelessfox.odradek.game.hfw.rtti.data;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.GGUUID;

import java.util.HexFormat;

public interface GGUUIDExtension {
    default String toDisplayString() {
        var gguuid = (GGUUID) this;
        var builder = new StringBuilder(36);
        var format = HexFormat.of();

        format.toHexDigits(builder, gguuid.data0());
        format.toHexDigits(builder, gguuid.data1());
        format.toHexDigits(builder, gguuid.data2());
        format.toHexDigits(builder, gguuid.data3()).append('-');
        format.toHexDigits(builder, gguuid.data4());
        format.toHexDigits(builder, gguuid.data5()).append('-');
        format.toHexDigits(builder, gguuid.data6());
        format.toHexDigits(builder, gguuid.data7()).append('-');
        format.toHexDigits(builder, gguuid.data8());
        format.toHexDigits(builder, gguuid.data9()).append('-');
        format.toHexDigits(builder, gguuid.data10());
        format.toHexDigits(builder, gguuid.data11());
        format.toHexDigits(builder, gguuid.data12());
        format.toHexDigits(builder, gguuid.data13());
        format.toHexDigits(builder, gguuid.data14());
        format.toHexDigits(builder, gguuid.data15());

        return builder.toString();
    }
}
