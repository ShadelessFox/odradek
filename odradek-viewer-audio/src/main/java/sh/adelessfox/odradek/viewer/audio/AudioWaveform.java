package sh.adelessfox.odradek.viewer.audio;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.audio.AudioFormat;

import javax.swing.*;
import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;

final class AudioWaveform extends JComponent {
    private static final Color PEAK_MAX = new Color(0xFF0000);
    private static final Color PEAK_MIN = new Color(0x0000FF);
    private static final Color PEAK_MID = new Color(0x800080);

    private static final Color PEAK_MAX_DIM = new Color(0x800000);
    private static final Color PEAK_MIN_DIM = new Color(0x000080);

    private static final int PEAK_WIDTH = 2;
    private static final int PEAK_GAP_H = 1;
    private static final int PEAK_GAP_V = 2;

    private final Audio audio;
    private final ShortBuffer data;
    private final List<Peak> peaks = new ArrayList<>();
    private float progress = 0.0f;

    public AudioWaveform(Audio audio) {
        this.audio = audio.toPcm16();
        this.data = ByteBuffer.wrap(this.audio.data())
            .order(ByteOrder.LITTLE_ENDIAN)
            .asShortBuffer();
    }

    @Override
    protected void paintComponent(Graphics g) {
        var g2 = (Graphics2D) g.create();

        int width = getWidth();
        int height = getHeight();
        int height2 = height / 2;

        int peaksTotal = width / (PEAK_WIDTH + PEAK_GAP_H);
        int samplesPerPeak = audio.samples() / peaksTotal;

        if (peaksTotal != peaks.size()) {
            peaks.clear();
            for (int i = 0; i < peaksTotal; i++) {
                peaks.add(computePeak(audio.format(), data, 0, samplesPerPeak * i, samplesPerPeak));
            }
        }

        // Background
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, width, height);

        var paint = new GradientPaint(0.0f, 0.0f, PEAK_MAX, 0.0f, height, PEAK_MIN);
        var paintDim = new GradientPaint(0.0f, 0.0f, PEAK_MAX_DIM, 0.0f, height, PEAK_MIN_DIM);

        // Peaks
        for (int i = 0; i < peaksTotal; i++) {
            if ((float) i / peaksTotal > progress) {
                g2.setPaint(paintDim);
            } else {
                g2.setPaint(paint);
            }

            var peak = peaks.get(i);

            int maxh = (int) (peak.max() * height2);
            g2.fillRect(i * (PEAK_WIDTH + PEAK_GAP_H), height2 - maxh - PEAK_GAP_V, PEAK_WIDTH, maxh);

            int minh = (int) (-peak.min() * height2);
            g2.fillRect(i * (PEAK_WIDTH + PEAK_GAP_H), height2 + PEAK_GAP_V, PEAK_WIDTH, minh);
        }

        // Midline
        g2.setColor(PEAK_MID);
        g2.fillRect(0, height / 2 - PEAK_GAP_V / 2, width, PEAK_GAP_V);

        g2.dispose();
    }

    public void setProgress(float progress) {
        if (this.progress != progress) {
            this.progress = progress;
            repaint();
        }
    }

    private static Peak computePeak(AudioFormat format, ShortBuffer buffer, int channel, int start, int count) {
        var statistics = new DoubleSummaryStatistics();
        for (int i = 0; i < count; i++) {
            int index = (start + i) * format.channels() + channel;
            var value = shortToFloat(buffer.get(index));
            statistics.accept(value);
        }
        return new Peak(
            (float) statistics.getMin(),
            (float) statistics.getMax(),
            (float) statistics.getAverage()
        );
    }

    private static float shortToFloat(short value) {
        return Math.max(value, -32767) / 32767.0f;
    }

    private record Peak(float min, float max, float avg) {
    }
}
