package sh.adelessfox.odradek.graphics;

import sh.adelessfox.odradek.graphics.win32.d3d.DxCompiler;
import sh.adelessfox.odradek.graphics.win32.d3d.IDxcCompiler;
import sh.adelessfox.odradek.graphics.win32.d3d.IDxcUtils;
import sh.adelessfox.odradek.util.system.OperatingSystem;

import java.lang.foreign.Arena;
import java.lang.foreign.SymbolLookup;

final class Disassembler {
    private Disassembler() {
    }

    static String disassemble(Program program) {
        if (OperatingSystem.name() != OperatingSystem.Name.WINDOWS) {
            throw new UnsupportedOperationException("Shader disassembly is only supported on Windows");
        }
        try (var arena = Arena.ofConfined()) {
            DxCompiler compiler;
            try {
                compiler = new DxCompiler(SymbolLookup.libraryLookup("dxcompiler", arena));
            } catch (IllegalArgumentException e) {
                throw new UnsupportedOperationException("Failed to load 'dxcompiler.dll'. Try placing it next to the application's executable", e);
            }
            try (
                var dxcUtils = compiler.createInstance(DxCompiler.CLSID_DxcUtils, IDxcUtils.IID_IDxcUtils);
                var dxcCompiler = compiler.createInstance(DxCompiler.CLSID_DxcCompiler, IDxcCompiler.IID_IDxcCompiler);
                var sourceBlob = dxcUtils.createBlob(program.blob(), 0);
                var disassemblyBlob = dxcCompiler.disassemble(sourceBlob)
            ) {
                return disassemblyBlob.getBuffer().getString(0);
            }
        }
    }
}
