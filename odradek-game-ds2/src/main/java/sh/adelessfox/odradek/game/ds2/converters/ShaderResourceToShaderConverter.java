package sh.adelessfox.odradek.game.ds2.converters;

import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.game.ds2.rtti.data.HwShader;
import sh.adelessfox.odradek.game.ds2.rtti.data.HwShaderProgram;
import sh.adelessfox.odradek.graphics.Program;
import sh.adelessfox.odradek.graphics.ProgramFormat;
import sh.adelessfox.odradek.graphics.ProgramType;
import sh.adelessfox.odradek.graphics.Shader;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

public final class ShaderResourceToShaderConverter implements Converter<DS2.ShaderResource, Shader, DS2Game> {
    @Override
    public Optional<Shader> convert(DS2.ShaderResource object, DS2Game game) {
        HwShader shader;

        try {
            byte[] data = game.readDataSource(object.streamingDataSource());
            shader = HwShader.read(BinaryReader.wrap(data));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        var programs = shader.programs().stream()
            .map(ShaderResourceToShaderConverter::mapProgram)
            .toList();

        return Optional.of(new Shader(programs));
    }

    private static Program mapProgram(HwShaderProgram program) {
        return new Program(
            mapProgramType(program.type()),
            mapProgramFormat(program),
            program.blob()
        );
    }

    private static ProgramType mapProgramType(DS2.EProgramType type) {
        return switch (type) {
            case Vertex -> ProgramType.VERTEX_PROGRAM;
            case Geometry -> ProgramType.GEOMETRY_PROGRAM;
            case Pixel -> ProgramType.PIXEL_PROGRAM;
            case Compute -> ProgramType.COMPUTE_PROGRAM;
        };
    }

    @SuppressWarnings("unused")
    private static ProgramFormat mapProgramFormat(HwShaderProgram program) {
        // Odradek only supports the PC release of DS2, so it's always DXBC
        return ProgramFormat.DXBC;
    }
}
