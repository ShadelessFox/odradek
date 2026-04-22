package sh.adelessfox.odradek.parsing;

/**
 * Represents a location in the source code, defined by a zero-based line and column number.
 *
 * @param line   the zero-based line number
 * @param column the zero-based column number
 */
public record Location(int line, int column) {
    public Location {
        if (line < 0) {
            throw new IllegalArgumentException("Line number cannot be negative");
        }
        if (column < 0) {
            throw new IllegalArgumentException("Column number cannot be negative");
        }
    }

    @Override
    public String toString() {
        return (line + 1) + ":" + (column + 1);
    }
}
