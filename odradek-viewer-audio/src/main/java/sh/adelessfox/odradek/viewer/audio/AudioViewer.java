package sh.adelessfox.odradek.viewer.audio;

import com.formdev.flatlaf.FlatClientProperties;
import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.data.DataContext;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.ui.Viewer;

import javax.swing.*;
import java.awt.BorderLayout;
import java.util.Optional;

public final class AudioViewer implements Viewer {
    public static final class Provider implements Viewer.Provider<Audio> {
        @Override
        public Viewer create(Audio object, Game game, Optional<?> selection, DataContext parentContext) {
            return new AudioViewer(object);
        }

        @Override
        public String name() {
            return "Audio";
        }

        @Override
        public Optional<String> icon() {
            return Optional.of("fugue:music");
        }
    }

    private final Audio audio;
    private JPanel component;
    private AudioPlayer player;
    private long position;
    private float volume = 1.0f;
    private boolean resume = true;
    private boolean disposed;

    private AudioViewer(Audio audio) {
        this.audio = audio;
    }

    @Override
    public JComponent createComponent() {
        if (component == null) {
            component = new JPanel(new BorderLayout());
            component.putClientProperty(FlatClientProperties.STYLE, "background: $Editor.background");
        }
        return component;
    }

    @Override
    public void activate() {
        if (disposed || player != null) {
            return;
        }
        // Keep only the encoded audio while inactive. Decoded audio and Clip buffers
        // can otherwise consume hundreds of megabytes for every open music tab.
        player = new AudioPlayer(audio, position, volume);
        player.putClientProperty(FlatClientProperties.STYLE, "background: $Editor.background");
        component.add(player, BorderLayout.CENTER);
        component.revalidate();
        component.repaint();
        if (resume) {
            player.start();
            resume = false;
        }
    }

    @Override
    public void deactivate() {
        if (player == null) {
            return;
        }
        resume = player.isPlaying();
        player.stop();
        position = player.getPosition();
        volume = player.getVolume();
        releasePlayer();
    }

    @Override
    public void dispose() {
        disposed = true;
        if (player != null) {
            releasePlayer();
        }
    }

    private void releasePlayer() {
        player.dispose();
        component.remove(player);
        player = null;
        component.revalidate();
        component.repaint();
    }
}
