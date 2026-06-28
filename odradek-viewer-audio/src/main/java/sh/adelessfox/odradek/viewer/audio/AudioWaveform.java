package sh.adelessfox.odradek.viewer.audio;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

final class AudioWaveform extends JComponent {
    private final SampleProvider sampler;
    private final List<Peak> peaks = new ArrayList<>();
    private float progress = 0.0f;

    // Styles
    private Color background;
    private Color peakBackground;
    private Color peakDimBackground;
    private int peakWidth;
    private int peakHorizontalGap;
    private int peakVerticalGap;

    public AudioWaveform(SampleProvider sampler) {
        this.sampler = sampler;
        updateUI();
    }

    @Override
    public void updateUI() {
        super.updateUI();

        background = UIManager.getColor("AudioWaveform.background");
        peakBackground = UIManager.getColor("AudioWaveform.peak.background");
        peakDimBackground = UIManager.getColor("AudioWaveform.peak.dim.background");
        peakWidth = UIManager.getInt("AudioWaveform.peak.width");
        peakHorizontalGap = UIManager.getInt("AudioWaveform.peak.horizontalGap");
        peakVerticalGap = UIManager.getInt("AudioWaveform.peak.verticalGap");
    }

    @Override
    protected void paintComponent(Graphics g) {
        var g2 = (Graphics2D) g.create();

        int width = getWidth();
        int height = getHeight();
        int height2 = height / 2;

        int peaksTotal = width / (peakWidth + peakHorizontalGap);
        int samplesPerPeak = sampler.getSampleCount() / peaksTotal;

        if (peaksTotal != peaks.size()) {
            peaks.clear();
            for (int i = 0; i < peaksTotal; i++) {
                // TODO: Show peaks of other channels as well
                peaks.add(computePeak(sampler, 0, samplesPerPeak * i, samplesPerPeak));
            }
        }

        // Background
        g2.setColor(background);
        g2.fillRect(0, 0, width, height);

        // Peaks
        for (int i = 0; i < peaksTotal; i++) {
            if ((float) i / peaksTotal > progress) {
                g2.setColor(peakDimBackground);
            } else {
                g2.setColor(peakBackground);
            }

            var peak = peaks.get(i);

            int maxh = (int) (peak.max() * height2);
            g2.fillRect(i * (peakWidth + peakHorizontalGap), height2 - maxh - peakVerticalGap, peakWidth, maxh);

            int minh = (int) (-peak.min() * height2);
            g2.fillRect(i * (peakWidth + peakHorizontalGap), height2 + peakVerticalGap, peakWidth, minh);
        }

        // Midline
        g2.setColor(peakBackground);
        g2.fillRect(0, height / 2 - peakVerticalGap / 2, width, peakVerticalGap);

        g2.dispose();
    }

    public void setProgress(float progress) {
        if (this.progress != progress) {
            this.progress = Math.clamp(progress, 0.0f, 1.0f);
            repaint();
        }
    }

    private static Peak computePeak(SampleProvider sampler, int channel, int start, int count) {
        var statistics = IntStream.range(start, start + count)
            .mapToDouble(i -> sampler.getSample(i, channel))
            .summaryStatistics();
        return new Peak(
            (float) statistics.getMin(),
            (float) statistics.getMax(),
            (float) statistics.getAverage()
        );
    }

    private record Peak(float min, float max, float avg) {
    }
}
