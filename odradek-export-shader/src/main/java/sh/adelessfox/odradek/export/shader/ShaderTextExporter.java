package sh.adelessfox.odradek.export.shader;

import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.graphics.Program;
import sh.adelessfox.odradek.graphics.Shader;
import sh.adelessfox.odradek.io.BinaryWriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class ShaderTextExporter extends BaseShaderExporter implements Exporter.OfMultipleOutputs<Shader> {
    @Override
    protected void write(Program program, BinaryWriter writer) throws IOException {
        try (var text = writer.asWriter(StandardCharsets.UTF_8)) {
            text.write(program.disassemble());
        }
    }

    @Override
    protected String extension() {
        return "txt";
    }

    @Override
    public String id() {
        return "shader.disassembled";
    }

    @Override
    public String name() {
        return "Shader (Disassembled, text)";
    }
}
