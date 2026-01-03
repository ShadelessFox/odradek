package sh.adelessfox.odradek.viewer.audio;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.ui.Viewer;

import javax.swing.*;
import java.awt.event.HierarchyEvent;
import java.util.Optional;

public final class AudioViewer implements Viewer<Audio> {
    @Override
    public JComponent createComponent(Audio audio) {
        var player = new AudioPlayer(audio);
        player.addHierarchyListener(e -> {
            if (e.getID() == HierarchyEvent.HIERARCHY_CHANGED && SwingUtilities.isDescendingFrom(player, e.getChanged())) {
                if (!player.isShowing()) {
                    player.stop();
                }
            }
        });
        return player;
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
