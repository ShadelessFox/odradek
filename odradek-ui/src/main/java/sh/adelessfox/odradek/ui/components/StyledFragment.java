package sh.adelessfox.odradek.ui.components;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import java.util.function.Consumer;

public record StyledFragment(
    String text,
    Optional<Color> background,
    Optional<Color> withForeground,
    boolean bold,
    boolean italic
) {
    public static final Consumer<Builder> NAME = b -> b.foreground(UIManager.getColor("StyledFragment.nameForeground"));
    public static final Consumer<Builder> NAME_DISABLED = b -> b.foreground(UIManager.getColor("StyledFragment.nameDisabledForeground"));
    public static final Consumer<Builder> NUMBER = b -> b.foreground(UIManager.getColor("StyledFragment.numberForeground"));
    public static final Consumer<Builder> STRING = b -> b.foreground(UIManager.getColor("StyledFragment.stringForeground"));
    public static final Consumer<Builder> GRAYED = b -> b.foreground(UIManager.getColor("Label.disabledForeground"));

    public static Builder builder() {
        return new Builder();
    }

    public static StyledFragment regular(String text) {
        return new StyledFragment(text, Optional.empty(), Optional.empty(), false, false);
    }

    public StyledFragment withForeground(Color color) {
        return new StyledFragment(text, background, Optional.of(color), bold, italic);
    }

    public static final class Builder {
        private Color background;
        private Color foreground;
        private boolean bold;
        private boolean italic;

        private Builder() {
        }

        public Builder background(Color background) {
            this.background = background;
            return this;
        }

        public Builder foreground(Color foreground) {
            this.foreground = foreground;
            return this;
        }

        public Builder bold() {
            this.bold = true;
            return this;
        }

        public Builder italic() {
            this.italic = true;
            return this;
        }

        public StyledFragment build(String text) {
            return new StyledFragment(
                text,
                Optional.ofNullable(background),
                Optional.ofNullable(foreground),
                bold,
                italic
            );
        }
    }
}
