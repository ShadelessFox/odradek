package sh.adelessfox.odradek.ui.actions;

import sh.adelessfox.odradek.data.DataContext;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class Action {
    public static Builder builder() {
        return new Builder();
    }

    public void perform(DataContext context) {
        // do nothing by default
    }

    public boolean isEnabled(DataContext context) {
        return isVisible(context);
    }

    public boolean isVisible(DataContext context) {
        return true;
    }

    public Optional<String> getText(DataContext context) {
        return Optional.empty();
    }

    public Optional<String> getDescription(DataContext context) {
        return Optional.empty();
    }

    public Optional<String> getIcon(DataContext context) {
        return Optional.empty();
    }

    public interface Check {
        boolean isChecked(DataContext context);
    }

    public interface Radio {
        boolean isSelected(DataContext context);
    }

    public static final class Builder {
        private Consumer<DataContext> perform;
        private Predicate<DataContext> isEnabled;
        private Predicate<DataContext> isVisible;
        private Function<DataContext, Optional<String>> textSupplier;
        private Function<DataContext, Optional<String>> descriptionSupplier;
        private Function<DataContext, Optional<String>> iconSupplier;

        private Builder() {
        }

        public Builder perform(Consumer<DataContext> perform) {
            this.perform = perform;
            return this;
        }

        public Builder isEnabled(Predicate<DataContext> isEnabled) {
            this.isEnabled = isEnabled;
            return this;
        }

        public Builder isVisible(Predicate<DataContext> isVisible) {
            this.isVisible = isVisible;
            return this;
        }

        public Builder text(Function<DataContext, Optional<String>> textSupplier) {
            this.textSupplier = textSupplier;
            return this;
        }

        public Builder description(Function<DataContext, Optional<String>> descriptionSupplier) {
            this.descriptionSupplier = descriptionSupplier;
            return this;
        }

        public Builder icon(Function<DataContext, Optional<String>> iconSupplier) {
            this.iconSupplier = iconSupplier;
            return this;
        }

        public Action build() {
            return new Action() {
                @Override
                public void perform(DataContext context) {
                    if (perform != null) {
                        perform.accept(context);
                    }
                }

                @Override
                public boolean isEnabled(DataContext context) {
                    return isEnabled == null || isEnabled.test(context);
                }

                @Override
                public boolean isVisible(DataContext context) {
                    return isVisible == null || isVisible.test(context);
                }

                @Override
                public Optional<String> getText(DataContext context) {
                    return textSupplier != null ? textSupplier.apply(context) : Optional.empty();
                }

                @Override
                public Optional<String> getDescription(DataContext context) {
                    return descriptionSupplier != null ? descriptionSupplier.apply(context) : Optional.empty();
                }

                @Override
                public Optional<String> getIcon(DataContext context) {
                    return iconSupplier != null ? iconSupplier.apply(context) : Optional.empty();
                }
            };
        }
    }
}
