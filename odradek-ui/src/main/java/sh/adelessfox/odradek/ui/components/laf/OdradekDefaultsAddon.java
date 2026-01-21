package sh.adelessfox.odradek.ui.components.laf;

import com.formdev.flatlaf.FlatDefaultsAddon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OdradekDefaultsAddon extends FlatDefaultsAddon {
    private static final Logger log = LoggerFactory.getLogger(OdradekDefaultsAddon.class);

    @Override
    public InputStream getDefaults(Class<?> lafClass) {
        return getClass().getResourceAsStream("/themes/%s.properties".formatted(lafClass.getSimpleName()));
    }

    @Override
    public void afterDefaultsLoading(LookAndFeel laf, UIDefaults defaults) {
        InputStream in = getClass().getResourceAsStream("/themes/styles.css");
        if (in == null) {
            return;
        }
        try (Reader reader = new InputStreamReader(in)) {
            String input = reader.readAllAsString();
            String output = processCss(input, defaults);
            new HTMLEditorKit().getStyleSheet().addRule(output);
        } catch (IOException e) {
            log.error("Failed to load CSS styles", e);
        }
    }

    private static String processCss(String input, UIDefaults defaults) {
        Pattern pattern = Pattern.compile("\\$([a-zA-Z0-9._]+)");
        Matcher matcher = pattern.matcher(input);
        return matcher.replaceAll(result -> processCssVariable(result.group(1), defaults));
    }

    private static String processCssVariable(String name, UIDefaults defaults) {
        var value = defaults.get(name);
        return switch (value) {
            case Color color -> "#%06x".formatted(color.getRGB() & 0xffffff);
            case null -> {
                log.warn("Undefined CSS variable '{}'", name);
                yield name;
            }
            default -> {
                log.warn("Unsupported CSS variable type for key '{}': {}", name, value.getClass().getName());
                yield name;
            }
        };
    }
}
