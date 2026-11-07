package sh.adelessfox.odradek.export.png.format;

import wtf.reversed.toolbox.collect.Bytes;

import java.util.List;
import java.util.stream.IntStream;

final class PngFilter {
    private final PngFormat format;
    private final List<Bytes.Mutable> filtered;
    private Bytes.Mutable previous;
    private Bytes.Mutable current;

    PngFilter(PngFormat format) {
        int length = format.bytesPerPixel() + format.bytesPerRow();
        this.filtered = IntStream.range(0, 5).mapToObj(_ -> Bytes.allocate(length)).toList();
        this.previous = Bytes.allocate(length);
        this.current = Bytes.allocate(length);
        this.format = format;
    }

    void reset() {
        previous.fill((byte) 0);
    }

    int filter(Bytes row, int offset) {
        int bpp = format.bytesPerPixel();
        int bpr = format.bytesPerRow();

        row.slice(offset, bpr).copyTo(current, bpp);
        int best = filter(current, previous, filtered, bpp, bpr);

        Bytes.Mutable temp = previous;
        previous = current;
        current = temp;
        return best;
    }

    Bytes.Mutable filtered(int filter) {
        return filtered.get(filter);
    }

    private static int filter(Bytes curr, Bytes prev, List<Bytes.Mutable> scratch, int bpp, int bpr) {
        var sRow = scratch.get(1);
        var uRow = scratch.get(2);
        var aRow = scratch.get(3);
        var pRow = scratch.get(4);
        int sadN = 0, sadS = 0, sadU = 0, sadA = 0, sadP = 0;
        for (int i = bpp; i < bpp + bpr; i++) {
            int x = curr.getUnsigned(i);
            int a = curr.getUnsigned(i - bpp);
            int b = prev.getUnsigned(i);
            int c = prev.getUnsigned(i - bpp);

            byte s = (byte) (x - a);
            byte u = (byte) (x - b);
            byte avg = (byte) (x - (a + b >>> 1));
            byte p = (byte) (x - paeth(a, b, c));
            sRow.set(i, s);
            uRow.set(i, u);
            aRow.set(i, avg);
            pRow.set(i, p);
            sadN += Math.abs((byte) x);
            sadS += Math.abs(s);
            sadU += Math.abs(u);
            sadA += Math.abs(avg);
            sadP += Math.abs(p);
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
            curr.slice(bpp, bpr).copyTo(scratch.get(0), bpp);
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
