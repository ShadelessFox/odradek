package sh.adelessfox.odradek.viewer.texture;

import net.miginfocom.swing.MigLayout;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.rtti.runtime.TypedObject;
import sh.adelessfox.odradek.texture.Texture;
import sh.adelessfox.odradek.texture.TextureFormat;
import sh.adelessfox.odradek.ui.Preview;
import sh.adelessfox.odradek.util.Arrays;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteOrder;

public final class TexturePreview implements Preview<TypedObject> {
    private static final int MAX_PREVIEW_SIZE = 256;

    @Override
    public JComponent createComponent(TypedObject object, Game game) {
        var panel = new JPanel();
        panel.setLayout(new MigLayout("ins panel,wrap"));

        var texture = Converter.convert(object, Texture.class, game).orElse(null);
        if (texture != null) {
            var image = createImage(texture);
            var view = new ImageView();
            view.setImage(image);
            view.setZoom((float) MAX_PREVIEW_SIZE / Math.max(image.getWidth(), image.getHeight()));

            panel.add(new JLabel(formatDescription(texture)));
            panel.add(new ImageViewport(view));
        } else {
            panel.add(new JLabel("Unable to preview texture"));
        }

        return panel;
    }

    @Override
    public boolean supports(TypeInfo info) {
        return Converter.converter(info, Texture.class).isPresent();
    }

    private static BufferedImage createImage(Texture texture) {
        var converted = texture.convert(TextureFormat.B8G8R8A8_UNORM);
        var surface = converted.surfaces().stream()
            .dropWhile(s -> s.width() > MAX_PREVIEW_SIZE || s.height() > MAX_PREVIEW_SIZE)
            .findFirst().orElseGet(() -> converted.surfaces().getLast());

        var image = new BufferedImage(surface.width(), surface.height(), BufferedImage.TYPE_INT_ARGB);
        var imageData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        for (int i = 0, o = 0, len = surface.data().length; i < len; i += 4, o++) {
            imageData[o] = Arrays.getInt(surface.data(), i, ByteOrder.LITTLE_ENDIAN);
        }

        return image;
    }

    private static String formatDescription(Texture texture) {
        var type = switch (texture.type()) {
            case SURFACE -> "2D";
            case VOLUME -> "3D";
            case ARRAY -> texture.duration().isPresent() ? "2D (Animated)" : "2D (Array)";
            case CUBEMAP -> "Cube Map";
        };
        return "%dx%d, %s, %s".formatted(texture.width(), texture.height(), texture.format(), type);
    }
}
