package sh.adelessfox.odradek.parsing.util;

import sh.adelessfox.odradek.parsing.Source;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;

public final class ReaderSource implements Source, Closeable {
    private static final int NOT_PEEKED = -2;

    private final Reader reader;

    private int peeked = NOT_PEEKED;
    private int line;
    private int column;

    public ReaderSource(Reader reader) {
        this.reader = reader;
    }

    @Override
    public int peek() {
        if (peeked == NOT_PEEKED) {
            peeked = readChar();
        }
        return peeked;
    }

    @Override
    public int next() {
        if (peeked == NOT_PEEKED) {
            return readChar();
        }
        int result = peeked;
        peeked = NOT_PEEKED;
        if (result == '\n') {
            line++;
            column = 0;
        } else {
            column++;
        }
        return result;
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public int column() {
        return column;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    private int readChar() {
        try {
            int high = reader.read();
            if (high == EOF || !Character.isSurrogate((char) high)) {
                return high;
            }
            if (Character.isLowSurrogate((char) high)) {
                throw new IOException("Unpaired low surrogate");
            }
            int low = reader.read();
            if (low == EOF || Character.isHighSurrogate((char) low)) {
                throw new IOException("Unpaired high surrogate");
            }
            return Character.toCodePoint((char) high, (char) low);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
