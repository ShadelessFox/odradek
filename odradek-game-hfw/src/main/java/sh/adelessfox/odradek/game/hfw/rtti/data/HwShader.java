package sh.adelessfox.odradek.game.hfw.rtti.data;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;
import java.util.List;

public record HwShader(
    int unk00,
    int unk04,
    HorizonForbiddenWest.EProgramTypeMask programTypeMask,
    int unk0C,
    int unk10,
    List<HwShaderProgram> programs,
    byte[] rootSignature
) {
    public HwShader {
        programs = List.copyOf(programs);
    }

    public static HwShader read(BinaryReader reader) throws IOException {
        var unk00 = reader.readInt();
        var unk04 = reader.readInt();
        var programTypeMask = HorizonForbiddenWest.EProgramTypeMask.valueOf(reader.readInt());
        var unk0C = reader.readInt();
        var unk10 = reader.readInt();
        var programs = reader.readObjects(reader.readInt(), HwShaderProgram::read);
        var rootSignature = reader.readBytes(reader.readInt());

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
