package sh.adelessfox.odradek.app.gl;

import org.lwjgl.system.MemoryStack;
import sh.adelessfox.odradek.math.Mat4f;
import sh.adelessfox.odradek.math.Vec3f;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public final class ShaderProgram implements GLObject {
    private final int program = glCreateProgram();
    private final Map<String, Integer> locations = new HashMap<>();

    public ShaderProgram(ShaderSource vertexShader, ShaderSource fragmentShader) {
        var vertexShaderId = compile(vertexShader, ShaderType.VERTEX);
        var fragmentShaderId = compile(fragmentShader, ShaderType.FRAGMENT);

        glAttachShader(program, vertexShaderId);
        glAttachShader(program, fragmentShaderId);
        glLinkProgram(program);

        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            throw new IllegalStateException("%s: %s".formatted(program, glGetProgramInfoLog(program)));
        }

        glDeleteShader(vertexShaderId);
        glDeleteShader(fragmentShaderId);
    }

    public void set(String name, int value) {
        ensureBound();
        glUniform1i(uniformLocation(name), value);
    }

    public void set(String name, float value) {
        ensureBound();
        glUniform1f(uniformLocation(name), value);
    }

    public void set(String name, Vec3f value) {
        ensureBound();
        glUniform3f(uniformLocation(name), value.x(), value.y(), value.z());
    }

    public void set(String name, Mat4f value) {
        ensureBound();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(Mat4f.BYTES / Float.BYTES);
            glUniformMatrix4fv(uniformLocation(name), false, value.get(buffer).flip());
        }
    }

    @Override
    public ShaderProgram bind() {
        glUseProgram(program);
        return this;
    }

    @Override
    public void unbind() {
        glUseProgram(0);
    }

    @Override
    public void dispose() {
        glDeleteProgram(program);
    }

    private int uniformLocation(String name) {
        return locations.computeIfAbsent(name, key -> {
            int location = glGetUniformLocation(program, key);
            if (location < 0) {
                throw new IllegalArgumentException("Can't find uniform '" + key + "' in program " + program);
            }
            return location;
        });
    }

    private int compile(ShaderSource shader, ShaderType type) {
        var id = glCreateShader(type.glType());

        glShaderSource(id, shader.source());
        glCompileShader(id);

        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new IllegalStateException("%s: %s".formatted(shader.name(), glGetShaderInfoLog(id)));
        }

        return id;
    }

    private void ensureBound() {
        if (glGetInteger(GL_CURRENT_PROGRAM) != program) {
            throw new IllegalArgumentException("Program is not bound");
        }
    }
}
