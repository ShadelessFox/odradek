package sh.adelessfox.odradek.export.png.format;

final class PngFilter {
    private final PngFormat format;
    private final byte[][] filtered;
    private byte[] previous;
    private byte[] current;

    PngFilter(PngFormat format) {
        this.filtered = new byte[5][format.bytesPerPixel() + format.bytesPerRow()];
        this.previous = new byte[format.bytesPerPixel() + format.bytesPerRow()];
        this.current = new byte[format.bytesPerPixel() + format.bytesPerRow()];
        this.format = format;
    }

    int filter(byte[] row, int offset) {
        int bpp = format.bytesPerPixel();
        int bpr = format.bytesPerRow();

        System.arraycopy(row, offset, current, bpp, bpr);
        int best = filter(current, previous, filtered, bpp, bpr);

        byte[] temp = previous;
        previous = current;
        current = temp;
        return best;
    }

    byte[] filtered(int filter) {
        return filtered[filter];
    }

    private static int filter(byte[] curr, byte[] prev, byte[][] scratch, int bpp, int bpr) {
        byte[] sRow = scratch[1];
        byte[] uRow = scratch[2];
        byte[] aRow = scratch[3];
        byte[] pRow = scratch[4];
        int sadN = 0, sadS = 0, sadU = 0, sadA = 0, sadP = 0;
        for (int i = bpp; i < bpp + bpr; i++) {
            int x = Byte.toUnsignedInt(curr[i]);
            int a = Byte.toUnsignedInt(curr[i - bpp]);
            int b = Byte.toUnsignedInt(prev[i]);
            int c = Byte.toUnsignedInt(prev[i - bpp]);

            sadN += Math.abs(curr[i]);
            sadS += Math.abs(sRow[i] = (byte) (x - a));
            sadU += Math.abs(uRow[i] = (byte) (x - b));
            sadA += Math.abs(aRow[i] = (byte) (x - (a + b >>> 1)));
            sadP += Math.abs(pRow[i] = (byte) (x - paeth(a, b, c)));
        }

        int[] sad = {sadN, sadS, sadU, sadA, sadP};
        int filter = 0;
        int minSad = sad[0];
        for (int i = 1; i < 5; i++) {
            if (sad[i] < minSad) {
                filter = i;
                minSad = sad[i];
            }
        }
        if (filter == 0) {
            System.arraycopy(curr, bpp, scratch[0], bpp, bpr);
        }
        return filter;
    }

    private static int paeth(int a, int b, int c) {
        var thresh = c * 3 - (a + b);
        var lo = Math.min(a, b);
        var hi = Math.max(a, b);
        var t0 = hi <= thresh ? lo : c;
        return thresh <= lo ? hi : t0;
    }
}
