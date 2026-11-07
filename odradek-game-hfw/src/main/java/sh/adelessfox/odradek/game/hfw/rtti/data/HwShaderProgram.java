package sh.adelessfox.odradek.game.hfw.rtti.data;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public record HwShaderProgram(
    int unk00,
    int unk04,
    int unk08,
    int unk0C,
    int unk10,
    HFW.EProgramType type,
    int unk18,
    int shaderModel,
    int unk20,
    int unk24,
    int unk28,
    int unk2C,
    int unk30,
    Bytes blob
) {
    public static HwShaderProgram read(BinarySource source) throws IOException {
        var unk00 = source.readInt();
        var unk04 = source.readInt();
        var unk08 = source.readInt();
        var unk0C = source.readInt();
        var unk10 = source.readInt();
        var type = HFW.EProgramType.valueOf(source.readInt());
        var unk18 = source.readInt();
        var shaderModel = source.readInt();
        var unk20 = source.readInt();
        var unk24 = source.readInt();
        var unk28 = source.readInt();
        var unk2C = source.readInt();
        var unk30 = source.readInt();
        var blob = source.readBytes(source.readInt());

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
