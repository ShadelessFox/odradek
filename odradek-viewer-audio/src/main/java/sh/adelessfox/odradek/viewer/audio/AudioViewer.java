package sh.adelessfox.odradek.viewer.audio;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.ui.Viewer;

import javax.swing.*;
import java.util.Optional;

public final class AudioViewer implements Viewer<Audio> {
    @Override
    public JComponent createComponent(Audio shader) {
        return new JLabel("Placeholder", SwingConstants.CENTER);
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
