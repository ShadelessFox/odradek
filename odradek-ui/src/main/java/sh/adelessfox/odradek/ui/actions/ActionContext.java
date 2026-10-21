package sh.adelessfox.odradek.ui.actions;

import sh.adelessfox.odradek.data.DataContext;
import sh.adelessfox.odradek.data.DataKey;

import java.awt.event.ActionEvent;
import java.util.Optional;

public record ActionContext(DataContext context, Object source, ActionEvent event) {
    public <T> Optional<T> get(DataKey<T> key) {
        return context.get(key);
    }

    public <T, R> Optional<R> get(DataKey<T> key, Class<R> type) {
        return context.get(key, type);
    }

    public <T> T require(DataKey<T> key) {
        return context.require(key);
    }

    public boolean has(DataKey<?> key) {
        return context.has(key);
    }

    public boolean has(DataKey<?> key, Class<?> type) {
        return context.has(key, type);
    }

    ActionContext withEvent(ActionEvent event) {
        return new ActionContext(context, source, event);
    }
}
