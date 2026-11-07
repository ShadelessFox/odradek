package sh.adelessfox.odradek.graphics;

import wtf.reversed.toolbox.collect.Bytes;

public record Program(ProgramType type, ProgramFormat format, Bytes blob) {
    /**
     * Disassembles the program into a human-readable format. The exact output depends on the program type and format.
     *
     * @return a human-readable disassembly of the program
     * @throws UnsupportedOperationException if the disassembler is not available or platform is not Windows
     */
    public String disassemble() {
        return Disassembler.disassemble(this);
    }
}
