package sh.adelessfox.odradek.game.hfw.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.EProgramType;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.ShaderResource;
import sh.adelessfox.odradek.game.hfw.rtti.data.HwShader;
import sh.adelessfox.odradek.game.hfw.rtti.data.HwShaderProgram;
import sh.adelessfox.odradek.graphics.Program;
import sh.adelessfox.odradek.graphics.ProgramFormat;
import sh.adelessfox.odradek.graphics.ProgramType;
import sh.adelessfox.odradek.graphics.Shader;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

public class ShaderConverter implements Converter<ForbiddenWestGame, Shader> {
    private static final Logger log = LoggerFactory.getLogger(ShaderConverter.class);

    @Override
    public Optional<Shader> convert(Object object, ForbiddenWestGame game) {
        HwShader shader;
        try {
            shader = HwShader.read(BinaryReader.wrap(((ShaderResource) object).data()));
        } catch (IOException e) {
            log.error("Error reading shader", e);
            return Optional.empty();
        }

        var programs = shader.programs().stream()
            .map(ShaderConverter::mapProgram)
            .toList();

        return Optional.of(new Shader(programs));
    }

    @Override
    public Set<Class<?>> supportedTypes() {
        return Set.of(ShaderResource.class);
    }

    private static Program mapProgram(HwShaderProgram program) {
        return new Program(
            mapProgramType(program.type()),
            mapProgramFormat(program),
            program.blob()
        );
    }

    private static ProgramType mapProgramType(EProgramType type) {
        return switch (type) {
            case VertexProgram -> ProgramType.VERTEX_PROGRAM;
            case GeometryProgram -> ProgramType.GEOMETRY_PROGRAM;
            case PixelProgram -> ProgramType.PIXEL_PROGRAM;
            case ComputeProgram -> ProgramType.COMPUTE_PROGRAM;
        };
    }

    @SuppressWarnings("unused")
    private static ProgramFormat mapProgramFormat(HwShaderProgram program) {
        // Odradek only supports the PC release of HFW, so it's always DXBC
        return ProgramFormat.DXBC;
    }
}
