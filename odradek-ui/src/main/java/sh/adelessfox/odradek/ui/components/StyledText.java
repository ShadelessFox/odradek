package sh.adelessfox.odradek.ui.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public record StyledText(List<StyledFragment> fragments) {
    public static Builder builder() {
        return new Builder();
    }

    public static StyledText of(String text) {
        return new StyledText(List.of(StyledFragment.regular(text)));
    }

    public StyledText {
        fragments = List.copyOf(fragments);
    }

    public Builder toBuilder() {
        return new Builder(fragments);
    }

    @Override
    public String toString() {
        return fragments.stream()
            .map(StyledFragment::text)
            .reduce("", String::concat);
    }

    public static final class Builder {
        private final List<StyledFragment> segments = new ArrayList<>();

        private Builder() {
        }

        private Builder(Collection<StyledFragment> c) {
            segments.addAll(c);
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

        public StyledText build() {
            return new StyledText(segments);
        }
    }

}
