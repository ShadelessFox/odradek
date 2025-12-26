package sh.adelessfox.odradek.game.hfw.rtti.data;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public record HwShaderProgram(
    int unk00,
    int unk04,
    int unk08,
    int unk0C,
    int unk10,
    HorizonForbiddenWest.EProgramType type,
    int unk18,
    int shaderModel,
    int unk20,
    int unk24,
    int unk28,
    int unk2C,
    int unk30,
    byte[] blob
) {
    public static HwShaderProgram read(BinaryReader reader) throws IOException {
        var unk00 = reader.readInt();
        var unk04 = reader.readInt();
        var unk08 = reader.readInt();
        var unk0C = reader.readInt();
        var unk10 = reader.readInt();
        var type = HorizonForbiddenWest.EProgramType.valueOf(reader.readInt());
        var unk18 = reader.readInt();
        var shaderModel = reader.readInt();
        var unk20 = reader.readInt();
        var unk24 = reader.readInt();
        var unk28 = reader.readInt();
        var unk2C = reader.readInt();
        var unk30 = reader.readInt();
        var blob = reader.readBytes(reader.readInt());

        return new HwShaderProgram(
            unk00,
            unk04,
            unk08,
            unk0C,
            unk10,
            type,
            unk18,
            shaderModel,
            unk20,
            unk24,
            unk28,
            unk2C,
            unk30,
            blob
        );
    }
}
