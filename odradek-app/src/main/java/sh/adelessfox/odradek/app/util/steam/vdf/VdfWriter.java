package sh.adelessfox.odradek.app.util.steam.vdf;

import java.io.IOException;

public final class VdfWriter {
    private VdfWriter() {
    }

    public static void write(VdfElement element, Appendable out) throws IOException {
        write(element, out, 0, false);
    }

    private static void write(VdfElement element, Appendable out, int indent, boolean nested) throws IOException {
        indent(out, indent);
        switch (element) {
            case VdfObject object -> write(object.getAsObject(), out, indent, nested);
            case VdfString string -> write(string.getAsString(), out);
            default -> throw new IllegalStateException();
        }
    }

    private static void write(VdfObject object, Appendable out, int indent, boolean nested) throws IOException {
        if (nested) {
            out.append('{');
            indent++;
        }
        for (var entry : object.entrySet()) {
            out.append('\n');
            indent(out, indent);
            write(entry.getKey(), out);
            if (entry.getValue() instanceof VdfObject) {
                out.append('\n');
                write(entry.getValue(), out, indent, true);
            } else {
                out.append('\t');
                write(entry.getValue(), out);
            }
        }
        if (nested) {
            indent--;
            out.append('\n');
            indent(out, indent);
            out.append('}');
        }
    }

    private static void write(String value, Appendable out) throws IOException {
        out.append('"');
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\n' -> out.append("\\n");
                case '\t' -> out.append("\\t");
                case '\\' -> out.append("\\\\");
                case '\"' -> out.append("\\\"");
                default -> out.append(ch);
            }
        }
        out.append('"');
    }

    private static void indent(Appendable out, int indent) throws IOException {
        for (int i = 0; i < indent; i++) {
            out.append('\t');
        }
    }
}
