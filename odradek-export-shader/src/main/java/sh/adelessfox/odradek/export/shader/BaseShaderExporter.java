package sh.adelessfox.odradek.export.shader;

import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.graphics.Program;
import sh.adelessfox.odradek.graphics.Shader;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.Optional;

abstract class BaseShaderExporter implements Exporter.OfMultipleOutputs<Shader> {
    @Override
    public void export(Shader object, OutputProvider provider) throws IOException {
        for (Program program : object.programs()) {
            var name = mapProgramFilename(program);
            write(program, provider.channel(name));
        }
    }

    protected abstract void write(Program program, WritableByteChannel channel) throws IOException;

    protected abstract String extension();

    @Override
    public Optional<String> icon() {
        return Optional.of("fugue:color");
    }

    private String mapProgramFilename(Program program) {
        var type = switch (program.type()) {
            case VERTEX_PROGRAM -> "vertex";
            case PIXEL_PROGRAM -> "pixel";
            case COMPUTE_PROGRAM -> "compute";
            case GEOMETRY_PROGRAM -> "geometry";
        };
        return type + '.' + extension();
    }
}
