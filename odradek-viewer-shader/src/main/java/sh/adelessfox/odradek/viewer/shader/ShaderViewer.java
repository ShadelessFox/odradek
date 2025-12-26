package sh.adelessfox.odradek.viewer.shader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.graphics.Program;
import sh.adelessfox.odradek.graphics.ProgramType;
import sh.adelessfox.odradek.graphics.Shader;
import sh.adelessfox.odradek.ui.Viewer;
import sh.adelessfox.odradek.viewer.shader.win32.d3d.DxCompiler;
import sh.adelessfox.odradek.viewer.shader.win32.d3d.IDxcCompiler;
import sh.adelessfox.odradek.viewer.shader.win32.d3d.IDxcUtils;

import javax.swing.*;
import java.lang.foreign.Arena;
import java.lang.foreign.SymbolLookup;
import java.util.Optional;

public class ShaderViewer implements Viewer<Shader> {
    private static final Logger log = LoggerFactory.getLogger(ShaderViewer.class);

    @Override
    public JComponent createComponent(Shader shader) {
        try (Arena arena = Arena.ofConfined()) {
            var lookup = SymbolLookup.libraryLookup("dxcompiler.dll", arena);
            var compiler = new DxCompiler(lookup);
            return createShaderPanel(shader, compiler);
        } catch (Exception e) {
            log.error("Unable to preview the shader", e);
            return new JLabel("Ensure 'dxcompiler.dll' is placed next to the application executable. Refer to log for more details", SwingConstants.CENTER);
        }
    }

    private static JComponent createShaderPanel(Shader shader, DxCompiler compiler) {
        var pane = new JTabbedPane();
        for (Program program : shader.programs()) {
            pane.add(toDisplayString(program.type()), createProgramPanel(program, compiler));
        }

        return pane;
    }

    private static JComponent createProgramPanel(Program program, DxCompiler compiler) {
        var area = new JTextArea();
        area.setFont(UIManager.getFont("monospaced.font"));
        area.setEditable(false);
        area.setText(disassemble(program, compiler));
        area.setCaretPosition(0);

        return new JScrollPane(area);
    }

    private static String disassemble(Program program, DxCompiler compiler) {
        try (
            var dxcUtils = compiler.createInstance(DxCompiler.CLSID_DxcUtils, IDxcUtils.IID_IDxcUtils);
            var dxcCompiler = compiler.createInstance(DxCompiler.CLSID_DxcCompiler, IDxcCompiler.IID_IDxcCompiler);
            var sourceBlob = dxcUtils.createBlob(program.blob(), 0);
            var disassemblyBlob = dxcCompiler.disassemble(sourceBlob)
        ) {
            return disassemblyBlob.getBuffer().getString(0);
        }
    }

    @Override
    public String name() {
        return "Shader";
    }

    @Override
    public Optional<String> icon() {
        return Optional.of("fugue:color");
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
