package sh.adelessfox.odradek.game.hfw.rtti.data;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;
import java.util.List;

public record HwShader(
    int unk00,
    int unk04,
    HFW.EProgramTypeMask programTypeMask,
    int unk0C,
    int unk10,
    List<HwShaderProgram> programs,
    Bytes rootSignature
) {
    public HwShader {
        programs = List.copyOf(programs);
    }

    public static HwShader read(BinarySource source) throws IOException {
        var unk00 = source.readInt();
        var unk04 = source.readInt();
        var programTypeMask = HFW.EProgramTypeMask.valueOf(source.readInt());
        var unk0C = source.readInt();
        var unk10 = source.readInt();
        var programs = source.readObjects(source.readInt(), HwShaderProgram::read);
        var rootSignature = source.readBytes(source.readInt());

        return new HwShader(
            unk00,
            unk04,
            programTypeMask,
            unk0C,
            unk10,
            programs,
            rootSignature
        );
    }
}
