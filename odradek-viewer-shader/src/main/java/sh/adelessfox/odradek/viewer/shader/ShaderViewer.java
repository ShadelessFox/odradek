package sh.adelessfox.odradek.viewer.shader;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.graphics.Program;
import sh.adelessfox.odradek.graphics.ProgramType;
import sh.adelessfox.odradek.graphics.Shader;
import sh.adelessfox.odradek.ui.Viewer;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

public record ShaderViewer(Shader shader, Game game) implements Viewer {
    public static final class Provider implements Viewer.Provider<Shader> {
        @Override
        public Viewer create(Shader object, Game game) {
            return new ShaderViewer(object, game);
        }

        @Override
        public String name() {
            return "Shader";
        }

        @Override
        public Optional<String> icon() {
            return Optional.of("fugue:color");
        }
    }

    @Override
    public JComponent createComponent() {
        var disassembled = shader.programs().stream()
            .map(Program::disassemble)
            .toList();

        return createShaderPanel(shader, disassembled);
    }

    private static JComponent createShaderPanel(Shader shader, List<String> disassembled) {
        var pane = new JTabbedPane();
        for (int i = 0; i < shader.programs().size(); i++) {
            var program = shader.programs().get(i);
            var text = disassembled.get(i);
            pane.add(toDisplayString(program.type()), createProgramPanel(text));
        }

        return pane;
    }

    private static JComponent createProgramPanel(String text) {
        var area = new JTextArea();
        area.setFont(UIManager.getFont("monospaced.font"));
        area.setEditable(false);
        area.setText(text);
        area.setCaretPosition(0);

        return new JScrollPane(area);
    }

    private static String toDisplayString(ProgramType type) {
        return switch (type) {
            case VERTEX_PROGRAM -> "Vertex Program";
            case PIXEL_PROGRAM -> "Pixel Program";
            case COMPUTE_PROGRAM -> "Compute Program";
            case GEOMETRY_PROGRAM -> "Geometry Program";
        };
    }
}
