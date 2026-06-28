package sh.adelessfox.odradek.viewer.audio;

import com.formdev.flatlaf.FlatClientProperties;
import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.ui.Viewer;

import javax.swing.*;
import java.util.Optional;

public final class AudioViewer implements Viewer {
    public static final class Provider implements Viewer.Provider<Audio> {
        @Override
        public Viewer create(Audio object, Game game) {
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
    private AudioPlayer player;
    private boolean resume = true;

    private AudioViewer(Audio audio) {
        this.audio = audio;
    }

    @Override
    public JComponent createComponent() {
        player = new AudioPlayer(audio);
        player.putClientProperty(FlatClientProperties.STYLE, "background: $Editor.background");

        return player;
    }

    @Override
    public void activate() {
        if (resume) {
            player.start();
            resume = false;
        }
    }

    @Override
    public void deactivate() {
        resume = player.isPlaying();
        player.stop();
    }

    @Override
    public void dispose() {
        player.dispose();
    }
}
