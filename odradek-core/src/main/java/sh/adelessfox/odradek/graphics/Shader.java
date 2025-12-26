package sh.adelessfox.odradek.graphics;

import java.util.List;

public record Shader(List<Program> programs) {
    public Shader {
        programs = List.copyOf(programs);
    }
}
