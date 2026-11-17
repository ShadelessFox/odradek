package sh.adelessfox.odradek.viewer.audio;

import com.formdev.flatlaf.FlatLightLaf;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.audio.AudioCodec;
import sh.adelessfox.odradek.audio.AudioFormat;
import sh.adelessfox.odradek.data.DataContext;
import sh.adelessfox.odradek.ui.Viewer;
import wtf.reversed.toolbox.collect.Bytes;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class AudioViewerTest {
    @BeforeAll
    static void setupLookAndFeel() throws Exception {
        SwingUtilities.invokeAndWait(() -> assertTrue(FlatLightLaf.setup()));
    }

    @Test
    void doesNotDecodeUntilActivatedAndCanBeDisposedBeforeActivation() throws Exception {
        // Invalid encoded data makes accidental eager decoding fail this test.
        var audio = new Audio(new AudioCodec.Wwise(), new AudioFormat(48000, 2), 48000, Bytes.empty());
        SwingUtilities.invokeAndWait(() -> {
            var viewer = viewer(audio);
            var component = viewer.createComponent();
            assertSame(component, viewer.createComponent());
            assertEquals(0, component.getComponentCount());
            viewer.deactivate();
            viewer.dispose();
            viewer.dispose();
            viewer.activate();
            assertEquals(0, component.getComponentCount());
        });
    }

    @Test
    void releasesInactivePlayerAndRestoresPausedPositionAndVolume() throws Exception {
        requireAudioDevice();
        var viewer = viewer(silence());
        JPanel[] componentHolder = new JPanel[1];
        SwingUtilities.invokeAndWait(() -> {
            componentHolder[0] = (JPanel) viewer.createComponent();
            viewer.activate();
            ((AudioPlayer) componentHolder[0].getComponent(0)).stop();
        });
        // Let Java Sound finish its in-flight buffer write after pausing.
        Thread.sleep(100);
        SwingUtilities.invokeAndWait(() -> {
            var component = componentHolder[0];
            try {
                var first = (AudioPlayer) component.getComponent(0);
                first.setPosition(500_000);
                first.setVolume(0.5f);
                long position = first.getPosition();
                assertEquals(500_000, position, 25);
                float volume = first.getVolume();

                viewer.deactivate();
                assertEquals(0, component.getComponentCount(), "Inactive tabs must release their decoded player");
                viewer.deactivate();
                viewer.activate();
                var second = (AudioPlayer) component.getComponent(0);
                assertNotSame(first, second);
                assertFalse(second.isPlaying(), "A paused preview must stay paused when revisited");
                assertEquals(position, second.getPosition());
                assertEquals(volume, second.getVolume(), 0.001f);
                viewer.activate();
                assertSame(second, component.getComponent(0));
            } finally {
                viewer.dispose();
            }
            assertEquals(0, component.getComponentCount());
        });
    }

    @Test
    void disposedPlayerIgnoresQueuedPlaybackEvents() throws Exception {
        requireAudioDevice();
        AudioPlayer[] player = new AudioPlayer[1];
        SwingUtilities.invokeAndWait(() -> {
            player[0] = new AudioPlayer(silence(), 0, 1.0f);
            player[0].start();
            player[0].dispose();
            player[0].dispose();
        });
        // Allow the asynchronous Java Sound callbacks and close operation to finish.
        Thread.sleep(2500);
        SwingUtilities.invokeAndWait(() -> {
            try {
                var timer = AudioPlayer.class.getDeclaredField("timer");
                timer.setAccessible(true);
                assertFalse(((Timer) timer.get(player[0])).isRunning());
                var clip = AudioPlayer.class.getDeclaredField("clip");
                clip.setAccessible(true);
                assertFalse(((Clip) clip.get(player[0])).isOpen());
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        });
    }

    private static Viewer viewer(Audio audio) {
        return new AudioViewer.Provider().create(audio, null, Optional.empty(), new DataContext());
    }

    private static Audio silence() {
        return new Audio(AudioCodec.Pcm.S16LE, new AudioFormat(48000, 2), 48000, Bytes.allocate(48000 * 4));
    }

    private static void requireAudioDevice() {
        assumeTrue(AudioSystem.isLineSupported(new DataLine.Info(Clip.class,
            new javax.sound.sampled.AudioFormat(48000, 16, 2, true, false))), "Requires a Java Sound playback device");
    }
}
