package sh.adelessfox.odradek.export.png.format;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.time.Duration;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

public final class PngWriter implements Closeable {
    // PNG signature
    private final byte[] SIGNATURE = new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', (byte) 0x1A, '\n'};

    // Chunk IDs
    private static final int IHDR = 'I' << 24 | 'H' << 16 | 'D' << 8 | 'R';
    private static final int IDAT = 'I' << 24 | 'D' << 16 | 'A' << 8 | 'T';
    private static final int IEND = 'I' << 24 | 'E' << 16 | 'N' << 8 | 'D';
    private static final int acTL = 'a' << 24 | 'c' << 16 | 'T' << 8 | 'L';
    private static final int fcTL = 'f' << 24 | 'c' << 16 | 'T' << 8 | 'L';
    private static final int fdAT = 'f' << 24 | 'd' << 16 | 'A' << 8 | 'T';

    private final PngFormat format;
    private final PngFilter filter;
    private final WritableByteChannel channel;
    private final int frames;
    private final int plays;

    private final Deflater deflater = new Deflater(Deflater.BEST_SPEED);
    private final byte[] idatBuffer = new byte[1024 * 64 - 12];
    private int idatLength;

    private int sequenceNumber;
    private int frameNumber;

    private PngWriter(PngFormat format, WritableByteChannel channel, int frames, int plays) {
        this.format = format;
        this.filter = new PngFilter(format);
        this.channel = channel;
        this.frames = frames;
        this.plays = plays;
    }

    public static PngWriter of(PngFormat format, WritableByteChannel channel) {
        return new PngWriter(format, channel, 0, 0);
    }

    public static PngWriter ofAnimated(PngFormat format, int frames, int plays, WritableByteChannel channel) {
        if (frames <= 0) {
            throw new IllegalArgumentException("frames must be greater than 0");
        }
        if (plays < 0) {
            throw new IllegalArgumentException("plays must be non-negative");
        }
        return new PngWriter(format, channel, frames, plays);
    }

    public void write(byte[] data) throws IOException {
        if (frames > 0) {
            throw new IllegalStateException("Can't write frame to animated PNG, " +
                "use write(byte[], Duration, PngDisposeMethod, PngBlendMethod) instead");
        }
        writeHeader();
        writeImage(data);
    }

    public void write(
        byte[] data,
        Duration duration,
        PngDisposeMethod dispose,
        PngBlendMethod blend
    ) throws IOException {
        write(data, 0, 0, format.width(), format.height(), duration, dispose, blend);
    }

    public void write(
        byte[] data,
        int x,
        int y,
        int width,
        int height,
        Duration duration,
        PngDisposeMethod dispose,
        PngBlendMethod blend
    ) throws IOException {
        if (frames == 0) {
            throw new IllegalStateException("Can't write frame to non-animated PNG, use write(byte[]) instead");
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
        if (frames > 0) {
            if (frameNumber == 0) {
                throw new IllegalStateException("No frames written");
            }
            if (frameNumber != frames) {
                throw new IllegalStateException("Expected " + frames + " frames, but only "
                    + frameNumber + " were written");
            }
        }
        writeIEND();
        deflater.end();
    }

    private void writeHeader() throws IOException {
        channel.write(ByteBuffer.wrap(SIGNATURE));
        writeIHDR();
    }

    private void writeImage(byte[] image) throws IOException {
        if (image.length != format.bytesPerImage()) {
            throw new IllegalArgumentException("image has wrong size, expected "
                + format.bytesPerImage() + " but was " + image.length);
        }
        for (int y = 0; y < format.height(); y++) {
            writeRow(image, y * format.bytesPerRow());
        }
        flush();
    }

    private void writeRow(byte[] image, int offset) throws IOException {
        if (offset + format.bytesPerRow() > image.length) {
            throw new IllegalArgumentException("image has wrong size, expected at least " +
                (offset + format.bytesPerRow()) + " but was " + image.length);
        }
        int filterMethod = filter.filter(image, offset);
        deflate(new byte[]{(byte) filterMethod}, 0, 1);
        deflate(filter.filtered(filterMethod), format.bytesPerPixel(), format.bytesPerRow());
    }

    private void deflate(byte[] array, int offset, int length) throws IOException {
        deflater.setInput(array, offset, length);
        while (!deflater.needsInput()) {
            deflate();
        }
    }

    private void deflate() throws IOException {
        int len = deflater.deflate(idatBuffer, idatLength, idatBuffer.length - idatLength);
        if (len > 0) {
            idatLength += len;
            if (idatLength == idatBuffer.length) {
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
        var data = ByteBuffer.allocate(13)
            .putInt(format.width())
            .putInt(format.height())
            .put(mapBitDepth(format))
            .put(mapColorType(format))
            .put((byte) 0) // compression method - deflate
            .put((byte) 0) // filter method - adaptive
            .put((byte) 0) // interlace method - no interlace
            .flip();

        writeChunk(IHDR, data);
    }

    private void writeIDAT() throws IOException {
        writeChunk(IDAT, ByteBuffer.wrap(idatBuffer, 0, idatLength));
        idatLength = 0;
    }

    private void writeFDAT(int sequenceNumber) throws IOException {
        var buffer = ByteBuffer.allocate(idatLength + 4)
            .putInt(sequenceNumber)
            .put(idatBuffer, 0, idatLength)
            .flip();

        writeChunk(fdAT, buffer);
        idatLength = 0;
    }

    private void writeIEND() throws IOException {
        writeChunk(IEND, ByteBuffer.allocate(0));
    }

    private void writeACTL(int frames, int plays) throws IOException {
        var data = ByteBuffer.allocate(8)
            .putInt(frames)
            .putInt(plays)
            .flip();

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
        var data = ByteBuffer.allocate(26)
            .putInt(sequenceNumber)
            .putInt(width)
            .putInt(height)
            .putInt(xOffset)
            .putInt(yOffset)
            .putShort((short) duration.toMillis())
            .putShort((short) 1000)
            .put(mapDisposeMethod(dispose))
            .put(mapBlendMethod(blend))
            .flip();

        writeChunk(fcTL, data);
    }

    private void writeChunk(int type, ByteBuffer data) throws IOException {
        var buffer = ByteBuffer.allocate(data.remaining() + 12)
            .putInt(data.remaining())
            .putInt(type)
            .put(data.slice());

        var crc32 = new CRC32();
        crc32.update(buffer.slice(4, data.remaining() + 4));

        buffer.putInt((int) crc32.getValue());
        buffer.flip();

        channel.write(buffer);
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
