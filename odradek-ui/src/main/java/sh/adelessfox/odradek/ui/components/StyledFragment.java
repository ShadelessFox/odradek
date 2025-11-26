package sh.adelessfox.odradek.ui.components;

import java.awt.*;
import java.util.Optional;

public record StyledFragment(
    String text,
    Optional<Color> background,
    Optional<Color> withForeground,
    boolean bold,
    boolean italic
) {
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
