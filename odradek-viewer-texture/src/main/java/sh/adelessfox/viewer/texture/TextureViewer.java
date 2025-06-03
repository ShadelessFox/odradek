package sh.adelessfox.viewer.texture;

import sh.adelessfox.odradek.texture.Texture;
import sh.adelessfox.odradek.ui.Viewer;

import javax.swing.*;

public class TextureViewer implements Viewer<Texture> {
    @Override
    public JComponent createPreview(Texture texture) {
        return new JLabel("Placeholder", SwingConstants.CENTER);
    }

    @Override
    public String displayName() {
        return "Texture";
    }
}
