package sh.adelessfox.odradek.viewer.audio;

import com.formdev.flatlaf.extras.components.FlatProgressBar;
import net.miginfocom.swing.MigLayout;
import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.audio.AudioCodec;
import sh.adelessfox.odradek.event.DefaultEventBus;
import sh.adelessfox.odradek.event.Event;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.ui.Disposable;
import sh.adelessfox.odradek.ui.components.SplitButton;
import sh.adelessfox.odradek.ui.util.Fugue;
import sh.adelessfox.odradek.util.Futures;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

final class AudioPlayer extends JPanel implements Disposable {
    private static final int REPAINT_INTERVAL = 1000 / 60; // TODO too arbitrary

    private final EventBus eventBus = new DefaultEventBus();
    private final Clip clip;

    public AudioPlayer(Audio audio) {
        // Both waveform and clip expects pcm16, so do it right away to avoid extra conversion
        var pcm16 = audio.convert(AudioCodec.Pcm.S16LE, 2);

        try {
            clip = openClip(pcm16);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        setLayout(new MigLayout("ins panel,wrap", "fill,grow", "[fill,grow][]"));
        add(createWaveform(pcm16, eventBus));
        add(createProgressBar(eventBus));
        add(createToolBar(audio, clip, eventBus));

        var timer = new Timer(REPAINT_INTERVAL, _ -> {
            var position = (float) clip.getMicrosecondPosition() / Math.max(1, clip.getMicrosecondLength());
            eventBus.publish(new Events.PositionChanged(position));
        });
        timer.setInitialDelay(0);

        eventBus.subscribe(Events.class, event -> {
            switch (event) {
                case Events.PlayRequested _ -> {
                    if (clip.getMicrosecondPosition() == clip.getMicrosecondLength()) {
                        clip.setMicrosecondPosition(0);
                    }
                    clip.start();
                }
                case Events.PauseRequested _ -> clip.stop();
                case Events.SetPositionRequested e -> {
                    var position = (long) (clip.getMicrosecondLength() * e.position());
                    clip.setMicrosecondPosition(position);
                    eventBus.publish(new Events.PositionChanged(e.position()));
                }
                case Events.SetVolumeRequested e -> {
                    var gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    gain.setValue(Math.clamp(linearToDb(e.volume()), gain.getMinimum(), gain.getMaximum()));
                }
                case Events.PlaybackStarted _ -> timer.start();
                case Events.PlaybackStopped _ -> timer.stop();
                case Events.PositionChanged _ -> { /* don't care; present only for exhaustiveness */ }
            }
        });

        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.START) {
                eventBus.publish(new Events.PlaybackStarted());
            } else if (event.getType() == LineEvent.Type.STOP) {
                eventBus.publish(new Events.PlaybackStopped());
            }
        });
    }

    public void start() {
        eventBus.publish(new Events.PlayRequested());
    }

    public void stop() {
        eventBus.publish(new Events.PauseRequested());
    }

    public boolean isPlaying() {
        return clip.isRunning();
    }

    @Override
    public void dispose() {
        // DirectClip#close may block for up to two seconds. Close it asynchronously so it won't block the UI
        Futures.submit(clip::close);
    }

    //region Components
    private static AudioWaveform createWaveform(Audio pcm16, EventBus eventBus) {
        var waveform = new AudioWaveform(pcm16);
        eventBus.subscribe(Events.PositionChanged.class, e -> waveform.setProgress(e.position()));
        return waveform;
    }

    private static FlatProgressBar createProgressBar(EventBus eventBus) {
        var progressBar = new FlatProgressBar();
        progressBar.setLargeHeight(true);
        progressBar.setMaximum(1000);

        var handler = new ScrubberHandler(progressBar, eventBus);
        progressBar.addMouseListener(handler);
        progressBar.addMouseMotionListener(handler);

        eventBus.subscribe(Events.PositionChanged.class, e -> {
            progressBar.setValue((int) (e.position() * progressBar.getMaximum()));
            progressBar.repaint(); // somehow is required ...
        });

        return progressBar;
    }

    private static JToolBar createToolBar(Audio audio, Clip clip, EventBus eventBus) {
        var toolBar = new JToolBar();
        toolBar.add(createPlayButton(eventBus));
        toolBar.add(Box.createHorizontalStrut(4));

        var duration = Duration.of(clip.getMicrosecondLength(), ChronoUnit.MICROS);
        toolBar.add(createProgressLabel(duration, eventBus));

        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            var gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            int min = (int) (dbToLinear(gain.getMinimum()) * 100);
            int max = (int) (dbToLinear(gain.getMaximum()) * 100);
            int set = (int) (dbToLinear(gain.getValue()) * 100);

            toolBar.add(Box.createHorizontalStrut(4));
            toolBar.add(createVolumeControl(min, max, set, eventBus));
        }

        toolBar.add(Box.createHorizontalGlue());
        toolBar.addSeparator();
        toolBar.add(Box.createHorizontalStrut(4));
        toolBar.add(new JLabel("%d Hz, %d channels".formatted(audio.format().sampleRate(), audio.format().channels())));
        toolBar.add(Box.createHorizontalStrut(4));

        return toolBar;
    }

    private static JLabel createProgressLabel(Duration duration, EventBus eventBus) {
        var label = new JLabel();
        eventBus.subscribe(
            Events.PositionChanged.class,
            e -> label.setText("%s / %s".formatted(
                formatDuration(Duration.ofMillis((long) (duration.toMillis() * e.position()))),
                formatDuration(duration))));
        return label;
    }

    private static JComponent createVolumeControl(int minGain, int maxGain, int currentGain, EventBus eventBus) {
        var label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.CENTER);

        var slider = new JSlider(minGain, maxGain, currentGain);
        slider.addChangeListener(_ -> {
            var volume = slider.getValue() / 100.0f;
            eventBus.publish(new Events.SetVolumeRequested(volume));
            label.setText("%d%%".formatted(slider.getValue()));
        });

        // Trigger the listener
        slider.setValue(minGain);
        slider.setValue(currentGain);

        var panel = new JPanel();
        panel.setLayout(new MigLayout("", "grow,fill"));
        panel.setOpaque(false);
        panel.add(label, "span,wrap");
        panel.add(new JLabel(Fugue.getIcon("speaker-volume-none")));
        panel.add(slider);
        panel.add(new JLabel(Fugue.getIcon("speaker-volume")));

        var button = new SplitButton();
        button.setIcon(Fugue.getIcon("speaker-volume"));
        button.setToolTipText("Volume");
        button.getPopupMenu().add(panel);

        return button;
    }

    private static PlayAction createPlayButton(EventBus eventBus) {
        return new PlayAction(eventBus);
    }
    //endregion

    private static String formatDuration(Duration duration) {
        return "%d:%02d.%03d".formatted(duration.toMinutes(), duration.toSecondsPart(), duration.toMillisPart());
    }

    private static float dbToLinear(double dB) {
        return (float) Math.pow(10.0, dB / 20.0);
    }

    private static float linearToDb(double linear) {
        return (float) (20.0 * Math.log10(linear));
    }

    private static Clip openClip(Audio audio) throws Exception {
        var pcm16 = audio.convert(AudioCodec.Pcm.S16LE);
        var format = new AudioFormat(pcm16.format().sampleRate(), 16, pcm16.format().channels(), true, false);
        var clip = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class, format));
        clip.open(format, pcm16.data(), 0, pcm16.data().length);
        return clip;
    }

    private sealed interface Events extends Event {
        record PlayRequested() implements Events {
        }

        record PauseRequested() implements Events {
        }

        record SetPositionRequested(float position) implements Events {
        }

        record SetVolumeRequested(float volume) implements Events {
        }

        record PlaybackStarted() implements Events {
        }

        record PlaybackStopped() implements Events {
        }

        record PositionChanged(float position) implements Events {
        }
    }

    private static class PlayAction extends AbstractAction {
        private final EventBus eventBus;
        private boolean playing;

        public PlayAction(EventBus eventBus) {
            this.eventBus = eventBus;

            eventBus.subscribe(Events.PlaybackStarted.class, _ -> setPlaying(true));
            eventBus.subscribe(Events.PlaybackStopped.class, _ -> setPlaying(false));
            setPlaying(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (playing) {
                eventBus.publish(new Events.PauseRequested());
            } else {
                eventBus.publish(new Events.PlayRequested());
            }
        }

        private void setPlaying(boolean playing) {
            putValue(Action.SMALL_ICON, Fugue.getIcon(playing ? "control-pause" : "control"));
            putValue(Action.SHORT_DESCRIPTION, playing ? "Pause" : "Play");
            this.playing = playing;
        }
    }

    private static class ScrubberHandler extends MouseAdapter {
        private final JComponent component;
        private final EventBus eventBus;

        ScrubberHandler(JComponent component, EventBus eventBus) {
            this.component = component;
            this.eventBus = eventBus;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            handle(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            handle(e);
        }

        private void handle(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                var position = Math.clamp(e.getPoint().x, 0.0f, component.getWidth()) / component.getWidth();
                eventBus.publish(new Events.SetPositionRequested(position));
            }
        }
    }
}
