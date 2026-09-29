package sh.adelessfox.odradek.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A text that consists of multiple fragments, each having its own styling,
 * like foreground and background color, font style, etc.
 *
 * @param fragments a list of fragments this styled text consists of
 */
public record StyledText(List<StyledFragment> fragments) {
    private static final StyledText EMPTY = new StyledText(List.of());

    public StyledText {
        fragments = List.copyOf(fragments);
    }

    public static Builder builder() {
        return new Builder("", "", "");
    }

    public static Builder builder(String delimiter) {
        return new Builder(delimiter, "", "");
    }

    public static Builder builder(String delimiter, String prefix, String suffix) {
        return new Builder(delimiter, prefix, suffix);
    }

    public static StyledText of() {
        return EMPTY;
    }

    public static StyledText of(String text) {
        return new StyledText(List.of(StyledFragment.regular(text)));
    }

    @Override
    public String toString() {
        return fragments.stream()
            .map(StyledFragment::text)
            .reduce("", String::concat);
    }

    public static final class Builder {
        private final List<StyledFragment> segments = new ArrayList<>(1);
        private final String delimiter;
        private final String prefix;
        private final String suffix;

        private Builder(String delimiter, String prefix, String suffix) {
            this.delimiter = delimiter;
            this.prefix = prefix;
            this.suffix = suffix;
        }

        public Builder add(String text) {
            segments.add(StyledFragment.regular(text));
            return this;
        }

        public Builder add(String text, Consumer<StyledFragment.Builder> handler) {
            var builder = StyledFragment.builder();
            handler.accept(builder);
            segments.add(builder.build(text));
            return this;
        }

        public Builder add(StyledText text) {
            segments.addAll(text.fragments());
            return this;
        }

        public boolean isEmpty() {
            return segments.isEmpty() && prefix.isEmpty() && suffix.isEmpty();
        }

        public Optional<StyledText> build() {
            if (segments.isEmpty()) {
                if (prefix.isEmpty() && suffix.isEmpty()) {
                    return Optional.empty();
                } else {
                    segments.add(StyledFragment.regular(prefix + suffix));
                }
            }
            var joined = new ArrayList<StyledFragment>(segments.size() * 2 - 1);
            if (!prefix.isEmpty()) {
                joined.add(StyledFragment.regular(prefix));
            }
            for (int i = 0; i < segments.size(); i++) {
                if (i > 0) {
                    joined.add(StyledFragment.regular(delimiter));
                }
                joined.add(segments.get(i));
            }
            if (!suffix.isEmpty()) {
                joined.add(StyledFragment.regular(suffix));
            }
            return Optional.of(new StyledText(joined));
        }
    }
}
