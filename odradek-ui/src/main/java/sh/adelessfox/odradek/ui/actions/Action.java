package sh.adelessfox.odradek.ui.actions;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class Action {
    public static Builder builder() {
        return new Builder();
    }

    public void perform(ActionContext context) {
        // do nothing by default
    }

    public boolean isEnabled(ActionContext context) {
        return isVisible(context);
    }

    public boolean isVisible(ActionContext context) {
        return true;
    }

    public Optional<String> getText(ActionContext context) {
        return Optional.empty();
    }

    public Optional<String> getDescription(ActionContext context) {
        return Optional.empty();
    }

    public Optional<String> getIcon(ActionContext context) {
        return Optional.empty();
    }

    public interface Check {
        boolean isChecked(ActionContext context);
    }

    public interface Radio {
        boolean isSelected(ActionContext context);
    }

    public static final class Builder {
        private Consumer<ActionContext> perform;
        private Predicate<ActionContext> isEnabled;
        private Predicate<ActionContext> isVisible;
        private Function<ActionContext, Optional<String>> textSupplier;
        private Function<ActionContext, Optional<String>> descriptionSupplier;
        private Function<ActionContext, Optional<String>> iconSupplier;

        private Builder() {
        }

        public Builder perform(Consumer<ActionContext> perform) {
            this.perform = perform;
            return this;
        }

        public Builder isEnabled(Predicate<ActionContext> isEnabled) {
            this.isEnabled = isEnabled;
            return this;
        }

        public Builder isVisible(Predicate<ActionContext> isVisible) {
            this.isVisible = isVisible;
            return this;
        }

        public Builder text(Function<ActionContext, Optional<String>> textSupplier) {
            this.textSupplier = textSupplier;
            return this;
        }

        public Builder description(Function<ActionContext, Optional<String>> descriptionSupplier) {
            this.descriptionSupplier = descriptionSupplier;
            return this;
        }

        public Builder icon(Function<ActionContext, Optional<String>> iconSupplier) {
            this.iconSupplier = iconSupplier;
            return this;
        }

        public Action build() {
            return new Action() {
                @Override
                public void perform(ActionContext context) {
                    if (perform != null) {
                        perform.accept(context);
                    }
                }

                @Override
                public boolean isEnabled(ActionContext context) {
                    return isEnabled == null || isEnabled.test(context);
                }

                @Override
                public boolean isVisible(ActionContext context) {
                    return isVisible == null || isVisible.test(context);
                }

                @Override
                public Optional<String> getText(ActionContext context) {
                    return textSupplier != null ? textSupplier.apply(context) : Optional.empty();
                }

                @Override
                public Optional<String> getDescription(ActionContext context) {
                    return descriptionSupplier != null ? descriptionSupplier.apply(context) : Optional.empty();
                }

                @Override
                public Optional<String> getIcon(ActionContext context) {
                    return iconSupplier != null ? iconSupplier.apply(context) : Optional.empty();
                }
            };
        }
    }
}
