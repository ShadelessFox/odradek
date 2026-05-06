package sh.adelessfox.odradek.graphics;

public record Program(ProgramType type, ProgramFormat format, byte[] blob) {
    /**
     * Disassembles the program into a human-readable format. The exact output depends on the program type and format.
     *
     * @return a human-readable disassembly of the program
     * @throws UnsupportedOperationException if the disassembler is not available or platform is not Windows
     */
    public String disassemble() throws UnsupportedOperationException {
        return Disassembler.disassemble(this);
    }
}
