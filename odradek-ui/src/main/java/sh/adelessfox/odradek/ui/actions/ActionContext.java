package sh.adelessfox.odradek.ui.actions;

import sh.adelessfox.odradek.ui.data.DataContext;
import sh.adelessfox.odradek.ui.data.DataKey;

import java.awt.event.ActionEvent;
import java.util.Optional;

public record ActionContext(DataContext context, Object source, ActionEvent event) implements DataContext {
    @Override
    public Optional<?> get(String key) {
        return context.get(key);
    }

    @Override
    public <T> Optional<T> get(DataKey<T> key) {
        return context.get(key);
    }

    @Override
    public <T, R> Optional<R> get(DataKey<T> key, Class<R> type) {
        return context.get(key, type);
    }

    ActionContext withEvent(ActionEvent event) {
        return new ActionContext(context, source, event);
    }
}
