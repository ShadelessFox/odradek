package sh.adelessfox.odradek.export.shader;

import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.graphics.Program;
import sh.adelessfox.odradek.graphics.Shader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public final class ShaderBlobExporter extends BaseShaderExporter implements Exporter.OfMultipleOutputs<Shader> {
    @Override
    protected void write(Program program, WritableByteChannel channel) throws IOException {
        channel.write(ByteBuffer.wrap(program.blob()));
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
