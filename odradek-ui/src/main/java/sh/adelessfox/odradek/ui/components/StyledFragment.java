package sh.adelessfox.odradek.ui.components;

import javax.swing.*;
import java.awt.*;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public record StyledFragment(
    String text,
    Optional<Color> background,
    Optional<Color> foreground,
    Set<StyledFlag> flags
) {
    // @formatter:off
    public static final Consumer<Builder> NAME = b -> b.foreground(UIManager.getColor("StyledFragment.nameForeground"));
    public static final Consumer<Builder> NUMBER = b -> b.foreground(UIManager.getColor("StyledFragment.numberForeground"));
    public static final Consumer<Builder> STRING = b -> b.foreground(UIManager.getColor("StyledFragment.stringForeground"));
    public static final Consumer<Builder> BOLD = Builder::bold;
    public static final Consumer<Builder> ITALIC = Builder::italic;
    public static final Consumer<Builder> GRAYED = Builder::grayed;
    // @formatter:on

    public StyledFragment {
        flags = Set.copyOf(flags);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static StyledFragment regular(String text) {
        return new StyledFragment(text, Optional.empty(), Optional.empty(), Set.of());
    }

    public StyledFragment withForeground(Color foreground) {
        return new StyledFragment(text, background, Optional.of(foreground), flags);
    }

    public StyledFragment withFlags(Set<StyledFlag> flags) {
        return new StyledFragment(text, background, foreground, flags);
    }

    public static final class Builder {
        private final Set<StyledFlag> flags = EnumSet.noneOf(StyledFlag.class);
        private Color background;
        private Color foreground;

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
            return flag(StyledFlag.BOLD);
        }

        public Builder italic() {
            return flag(StyledFlag.ITALIC);
        }

        public Builder grayed() {
            return flag(StyledFlag.GRAYED);
        }

        private Builder flag(StyledFlag flag) {
            flags.add(flag);
            return this;
        }

        public StyledFragment build(String text) {
            return new StyledFragment(
                text,
                Optional.ofNullable(background),
                Optional.ofNullable(foreground),
                flags
            );
        }
    }
}
