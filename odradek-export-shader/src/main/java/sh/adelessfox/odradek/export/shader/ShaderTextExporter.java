package sh.adelessfox.odradek.export.shader;

import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.graphics.Program;
import sh.adelessfox.odradek.graphics.Shader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;

public final class ShaderTextExporter extends BaseShaderExporter implements Exporter.OfMultipleOutputs<Shader> {
    @Override
    protected void write(Program program, WritableByteChannel channel) throws IOException {
        channel.write(ByteBuffer.wrap(program.disassemble().getBytes(StandardCharsets.UTF_8)));
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
