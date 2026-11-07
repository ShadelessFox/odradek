package sh.adelessfox.odradek.export.png.format;

import sh.adelessfox.odradek.io.BinaryWriter;
import wtf.reversed.toolbox.collect.Bytes;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

public final class PngWriter implements Closeable {
    // PNG signature
    private static final Bytes SIGNATURE = Bytes.wrap(new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', (byte) 0x1A, '\n'});

    // Chunk IDs
    private static final int IHDR = 'I' << 24 | 'H' << 16 | 'D' << 8 | 'R';
    private static final int IDAT = 'I' << 24 | 'D' << 16 | 'A' << 8 | 'T';
    private static final int IEND = 'I' << 24 | 'E' << 16 | 'N' << 8 | 'D';
    private static final int acTL = 'a' << 24 | 'c' << 16 | 'T' << 8 | 'L';
    private static final int fcTL = 'f' << 24 | 'c' << 16 | 'T' << 8 | 'L';
    private static final int fdAT = 'f' << 24 | 'd' << 16 | 'A' << 8 | 'T';
    private static final int sRGB = 's' << 24 | 'R' << 16 | 'G' << 8 | 'B';
    private static final int gAMA = 'g' << 24 | 'A' << 16 | 'M' << 8 | 'A';
    private static final int tEXt = 't' << 24 | 'E' << 16 | 'X' << 8 | 't';

    private final PngFormat format;
    private final PngFilter filter;
    private final BinaryWriter writer;
    private final int frames;
    private final int plays;

    private final Deflater deflater = new Deflater(Deflater.BEST_SPEED);
    private final Bytes.Mutable idatBuffer = Bytes.allocate(1024 * 64 - 12);
    private final Bytes.Mutable filterMethodBuffer = Bytes.allocate(1);
    private int idatLength;

    private int sequenceNumber;
    private int frameNumber;

    private PngWriter(PngFormat format, BinaryWriter writer, int frames, int plays) {
        this.format = format;
        this.filter = new PngFilter(format);
        this.writer = writer;
        this.frames = frames;
        this.plays = plays;
    }

    public static PngWriter of(PngFormat format, BinaryWriter writer) {
        return new PngWriter(format, writer, 0, 0);
    }

    public static PngWriter ofAnimated(PngFormat format, int frames, int plays, BinaryWriter writer) {
        if (frames <= 0) {
            throw new IllegalArgumentException("frames must be greater than 0");
        }
        if (plays < 0) {
            throw new IllegalArgumentException("plays must be non-negative");
        }
        return new PngWriter(format, writer, frames, plays);
    }

    public void write(Bytes data) throws IOException {
        if (frames > 0) {
            throw new IllegalStateException("Can't write frame to animated PNG, " +
                "use write(Bytes, Duration, PngDisposeMethod, PngBlendMethod) instead");
        }
        writeHeader();
        writeImage(data);
    }

    public void write(
        Bytes data,
        Duration duration,
        PngDisposeMethod dispose,
        PngBlendMethod blend
    ) throws IOException {
        write(data, 0, 0, format.width(), format.height(), duration, dispose, blend);
    }

    public void write(
        Bytes data,
        int x,
        int y,
        int width,
        int height,
        Duration duration,
        PngDisposeMethod dispose,
        PngBlendMethod blend
    ) throws IOException {
        if (frames == 0) {
            throw new IllegalStateException("Can't write frame to non-animated PNG, use write(Bytes) instead");
        }
        if (frameNumber >= frames) {
            throw new IllegalStateException("Already wrote " + frames + " frames, can't write more");
        }
        if (sequenceNumber == 0) {
            writeHeader();
            writeACTL(frames, plays);
        }
        writeFCTL(sequenceNumber++, width, height, x, y, duration, dispose, blend);
        writeImage(data);
        frameNumber++;
    }

    @Override
    public void close() throws IOException {
        try {
            validate();
            writeIEND();
        } finally {
            deflater.end();
        }
    }

    private void validate() {
        if (frames > 0) {
            if (frameNumber == 0) {
                throw new IllegalStateException("No frames written");
            }
            if (frameNumber != frames) {
                throw new IllegalStateException("Expected " + frames + " frames, but only "
                    + frameNumber + " were written");
            }
        }
    }

    private void writeHeader() throws IOException {
        writer.writeBytes(SIGNATURE);
        writeIHDR();
        if (format.colorSpace() == PngColorSpace.SRGB) {
            writeSRGB();
            writeGAMA();
        }
        writeTEXt("Software", "Odradek");
    }

    private void writeImage(Bytes image) throws IOException {
        if (image.length() != format.bytesPerImage()) {
            throw new IllegalArgumentException("image has wrong size, expected "
                + format.bytesPerImage() + " but was " + image.length());
        }
        filter.reset();
        for (int y = 0; y < format.height(); y++) {
            writeRow(image, y * format.bytesPerRow());
        }
        flush();
    }

    private void writeRow(Bytes image, int offset) throws IOException {
        if (offset + format.bytesPerRow() > image.length()) {
            throw new IllegalArgumentException("image has wrong size, expected at least " +
                (offset + format.bytesPerRow()) + " but was " + image.length());
        }
        int filterMethod = filter.filter(image, offset);
        deflate(filterMethodBuffer.set(0, (byte) filterMethod));
        deflate(filter.filtered(filterMethod).slice(format.bytesPerPixel(), format.bytesPerRow()));
    }

    private void deflate(Bytes data) throws IOException {
        deflater.setInput(data.asBuffer());
        while (!deflater.needsInput()) {
            deflate();
        }
    }

    private void deflate() throws IOException {
        int len = deflater.deflate(idatBuffer.asMutableBuffer().position(idatLength));
        if (len > 0) {
            idatLength += len;
            if (idatLength == idatBuffer.length()) {
                writeData();
            }
        }
    }

    private void flush() throws IOException {
        deflater.finish();
        while (!deflater.finished()) {
            deflate();
        }
        deflater.reset();
        writeData();
    }

    private void writeData() throws IOException {
        if (frames > 0 && frameNumber > 0) {
            writeFDAT(sequenceNumber++);
        } else {
            writeIDAT();
        }
    }

    private void writeIHDR() throws IOException {
        var data = Bytes.allocate(13)
            .setInt(0x00, Integer.reverseBytes(format.width()))
            .setInt(0x04, Integer.reverseBytes(format.height()))
            .set(0x08, mapBitDepth(format))
            .set(0x09, mapColorType(format))
            .set(0x0A, (byte) 0) // compression method - deflate
            .set(0x0B, (byte) 0) // filter method - adaptive
            .set(0x0C, (byte) 0); // interlace method - no interlace

        writeChunk(IHDR, data);
    }

    private void writeIDAT() throws IOException {
        writeChunk(IDAT, idatBuffer.slice(0, idatLength));
        idatLength = 0;
    }

    private void writeFDAT(int sequenceNumber) throws IOException {
        var buffer = Bytes.allocate(idatLength + 4)
            .setInt(0, Integer.reverseBytes(sequenceNumber));
        idatBuffer.slice(0, idatLength).copyTo(buffer, 4);

        writeChunk(fdAT, buffer);
        idatLength = 0;
    }

    private void writeSRGB() throws IOException {
        var data = Bytes.allocate(1)
            .set(0x00, (byte) 0); // rendering intent - perceptual

        writeChunk(sRGB, data);
    }

    private void writeGAMA() throws IOException {
        var data = Bytes.allocate(4)
            .setInt(0, Integer.reverseBytes(45455)); // gamma value - 1/2.2

        writeChunk(gAMA, data);
    }

    private void writeTEXt(String keyword, String text) throws IOException {
        var keywordBytes = Bytes.wrap(keyword.getBytes(StandardCharsets.ISO_8859_1));
        var textBytes = Bytes.wrap(text.getBytes(StandardCharsets.ISO_8859_1));

        var data = Bytes.allocate(keywordBytes.length() + 1 + textBytes.length());
        keywordBytes.copyTo(data, 0);
        textBytes.copyTo(data, keywordBytes.length() + 1);

        writeChunk(tEXt, data);
    }

    private void writeIEND() throws IOException {
        writeChunk(IEND, Bytes.empty());
    }

    private void writeACTL(int frames, int plays) throws IOException {
        var data = Bytes.allocate(8)
            .setInt(0x00, Integer.reverseBytes(frames))
            .setInt(0x04, Integer.reverseBytes(plays));

        writeChunk(acTL, data);
    }

    private void writeFCTL(
        int sequenceNumber,
        int width,
        int height,
        int xOffset,
        int yOffset,
        Duration duration,
        PngDisposeMethod dispose,
        PngBlendMethod blend
    ) throws IOException {
        var data = Bytes.allocate(26)
            .setInt(0x00, Integer.reverseBytes(sequenceNumber))
            .setInt(0x04, Integer.reverseBytes(width))
            .setInt(0x08, Integer.reverseBytes(height))
            .setInt(0x0C, Integer.reverseBytes(xOffset))
            .setInt(0x10, Integer.reverseBytes(yOffset))
            .setShort(0x14, Short.reverseBytes((short) duration.toMillis()))
            .setShort(0x16, Short.reverseBytes((short) 1000))
            .set(0x18, mapDisposeMethod(dispose))
            .set(0x19, mapBlendMethod(blend));

        writeChunk(fcTL, data);
    }

    private void writeChunk(int type, Bytes data) throws IOException {
        var buffer = Bytes.allocate(data.length() + 12)
            .setInt(0, Integer.reverseBytes(data.length()))
            .setInt(4, Integer.reverseBytes(type));
        data.copyTo(buffer, 8);

        var crc32 = new CRC32();
        crc32.update(buffer.slice(4, data.length() + 4).asBuffer());

        buffer.setInt(data.length() + 8, Integer.reverseBytes((int) crc32.getValue()));

        writer.writeBytes(buffer);
    }

    private static byte mapBitDepth(PngFormat format) {
        return (byte) format.colorType().bitDepth();
    }

    private static byte mapColorType(PngFormat format) {
        return switch (format.colorType()) {
            case PngColorType.Greyscale(boolean alpha, _) -> (byte) (alpha ? 4 : 0);
            case PngColorType.TrueColor(boolean alpha, _) -> (byte) (alpha ? 6 : 2);
        };
    }

    private static byte mapDisposeMethod(PngDisposeMethod method) {
        return switch (method) {
            case NONE -> 0;
            case BACKGROUND -> 1;
            case PREVIOUS -> 2;
        };
    }

    private static byte mapBlendMethod(PngBlendMethod method) {
        return switch (method) {
            case SOURCE -> 0;
            case OVER -> 1;
        };
    }
}
