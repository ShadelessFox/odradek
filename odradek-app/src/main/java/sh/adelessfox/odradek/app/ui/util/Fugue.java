package sh.adelessfox.odradek.app.ui.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Fugue {
    private static final Fugue instance;

    private final BufferedImage sheet;
    private final Map<String, Rectangle> locations;
    private final Map<String, IconDescriptor> cache;

    static {
        try {
            instance = load();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Fugue(BufferedImage sheet, Map<String, Rectangle> locations) {
        this.sheet = sheet;
        this.locations = Map.copyOf(locations);
        this.cache = new ConcurrentHashMap<>();
    }

    public static Icon getIcon(String name) {
        return instance.getIconDescriptor(name).icon();
    }

    public static Image getImage(String name) {
        return instance.getIconDescriptor(name).image();
    }

    private static Fugue load() throws IOException {
        try (
            InputStream iconsInputStream = Fugue.class.getResourceAsStream("fugue.png");
            InputStream namesInputStream = Fugue.class.getResourceAsStream("fugue.txt")
        ) {
            if (namesInputStream == null || iconsInputStream == null) {
                throw new IOException("Failed to load Fugue sheet");
            }

            var sheet = ImageIO.read(iconsInputStream);
            var names = new BufferedReader(new InputStreamReader(namesInputStream)).lines().toList();

            var size = 16;
            var stride = sheet.getWidth() / size;

            var locations = new HashMap<String, Rectangle>();
            for (int i = 0; i < names.size(); i++) {
                var name = names.get(i);
                var x = i % stride * size;
                var y = i / stride * size;
                locations.put(name, new Rectangle(x, y, size, size));
            }

            return new Fugue(sheet, locations);
        }
    }

    private IconDescriptor getIconDescriptor(String name) {
        return cache.computeIfAbsent(name, key -> {
            var location = locations.get(key);
            if (location == null) {
                throw new IllegalArgumentException("Unknown icon: " + key);
            }
            var image = sheet.getSubimage(location.x, location.y, location.width, location.height);
            var icon = new ImageIcon(image);
            return new IconDescriptor(image, icon);
        });
    }

    private record IconDescriptor(Image image, Icon icon) {
    }
}
