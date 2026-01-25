package sh.adelessfox.odradek.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A text that consists of multiple fragment each having its own styling,
 * like foreground and background color, font style, etc.
 *
 * @param fragments a list of fragments this styled text consists of
 */
public record StyledText(List<StyledFragment> fragments) {
    public StyledText {
        fragments = List.copyOf(fragments);
    }

    public static Builder builder() {
        return new Builder();
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

        private Builder() {
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
            return segments.isEmpty();
        }

        public Optional<StyledText> build() {
            if (segments.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new StyledText(segments));
        }
    }

}
