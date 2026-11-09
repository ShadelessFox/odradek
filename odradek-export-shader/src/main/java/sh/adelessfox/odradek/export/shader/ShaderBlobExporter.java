package sh.adelessfox.odradek.export.shader;

import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.graphics.Program;
import sh.adelessfox.odradek.graphics.Shader;
import sh.adelessfox.odradek.io.BinaryWriter;

import java.io.IOException;

public final class ShaderBlobExporter extends BaseShaderExporter implements Exporter.OfMultipleOutputs<Shader> {
    @Override
    protected void write(Program program, BinaryWriter writer) throws IOException {
        writer.writeBytes(program.blob());
    }

    @Override
    protected String extension() {
        return "bin";
    }

    @Override
    public String id() {
        return "shader.compiled";
    }

    @Override
    public String name() {
        return "Shader (Assembled, binary)";
    }
}
