package sh.adelessfox.odradek.viewer.audio;

import net.miginfocom.swing.MigLayout;
import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.ui.Disposable;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;

final class AudioPlayer extends JPanel implements Disposable {
    private final AudioWaveform waveform;
    private final JProgressBar progress;
    private final JLabel label;

    private final Clip clip;

    public AudioPlayer(Audio audio) {
        progress = new JProgressBar();
        progress.setStringPainted(true);
        progress.setString("");

        var handler = new ScrubberHandler();
        progress.addMouseListener(handler);
        progress.addMouseMotionListener(handler);

        waveform = new AudioWaveform(audio);
        waveform.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));

        var playAction = new PlayAction();
        var skipBackAction = new SkipAction(false);
        var skipNextAction = new SkipAction(true);

        label = new JLabel("00:00", SwingConstants.LEADING);

        var toolBar = new JToolBar();
        toolBar.setBorder(BorderFactory.createEmptyBorder());
        toolBar.add(skipBackAction);
        toolBar.add(playAction);
        toolBar.add(skipNextAction);
        toolBar.add(Box.createHorizontalStrut(4));
        toolBar.add(label);

        setLayout(new MigLayout("ins panel,wrap", "fill,grow", "[fill,grow][]"));
        add(waveform);
        add(toolBar);
        add(progress);

        try {
            clip = openClip(audio);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        var timer = new Timer(1000 / 60, _ -> updateProgress());
        timer.setInitialDelay(0);

        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.START) {
                timer.start();
                playAction.setPlaying(true);
            } else if (event.getType() == LineEvent.Type.STOP) {
                timer.stop();
                playAction.setPlaying(false);
            }
            updateProgress();
        });

        setProgress(Duration.ZERO, Duration.ZERO);
        start();
    }

    public void start() {
        if (clip.getMicrosecondPosition() == clip.getMicrosecondLength()) {
            clip.setMicrosecondPosition(0);
        }
        clip.start();
    }

    public void stop() {
        clip.stop();
    }

    @Override
    public void dispose() {
        clip.close();
    }

    private void setProgress(Duration position, Duration duration) {
        int value = duration.isZero() ? 0 : Math.toIntExact(position.multipliedBy(100).dividedBy(duration));
        progress.setValue(value);
        waveform.setProgress(value / 100.0f);
        label.setText("%s / %s".formatted(formatDuration(position), formatDuration(duration)));
    }

    private static String formatDuration(Duration duration) {
        return "%d:%02d".formatted(duration.toMinutes(), duration.toSecondsPart());
    }

    private static Clip openClip(Audio audio) throws Exception {
        var pcm16 = audio.toPcm16();
        var format = new AudioFormat(pcm16.format().sampleRate(), 16, pcm16.format().channels(), true, false);
        var clip = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class, format));
        clip.open(format, pcm16.data(), 0, pcm16.data().length);
        return clip;
    }

    private void updateProgress() {
        var position = Duration.ofMillis(clip.getMicrosecondPosition() / 1000);
        var duration = Duration.ofMillis(clip.getMicrosecondLength() / 1000);
        setProgress(position, duration);
    }

    private class SkipAction extends AbstractAction {
        private final boolean next;

        public SkipAction(boolean next) {
            this.next = next;
            putValue(SMALL_ICON, Fugue.getIcon(next ? "control-skip" : "control-skip-180"));
            putValue(SHORT_DESCRIPTION, next ? "Skip back" : "Skip next");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }

    private class PlayAction extends AbstractAction {
        private boolean playing;

        public PlayAction() {
            setPlaying(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (playing) {
                stop();
            } else {
                start();
            }
        }

        public void setPlaying(boolean playing) {
            putValue(SMALL_ICON, Fugue.getIcon(playing ? "control-pause" : "control"));
            putValue(SHORT_DESCRIPTION, playing ? "Pause" : "Play");
            this.playing = playing;
        }
    }

    private class ScrubberHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            handle(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            handle(e);
        }

        private void handle(MouseEvent e) {
            if (clip == null || !SwingUtilities.isLeftMouseButton(e)) {
                return;
            }

            float position = Math.clamp(e.getPoint().x, 0.0f, progress.getWidth());
            long microseconds = (long) (position / progress.getWidth() * clip.getMicrosecondLength());

            clip.setMicrosecondPosition(microseconds);
            updateProgress();
        }
    }
}
