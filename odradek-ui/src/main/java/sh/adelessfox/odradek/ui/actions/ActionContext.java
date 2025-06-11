package sh.adelessfox.odradek.ui.actions;

import sh.adelessfox.odradek.ui.data.DataContext;
import sh.adelessfox.odradek.ui.data.DataKey;

import java.awt.event.ActionEvent;
import java.util.Optional;

public record ActionContext(DataContext context, Object source, ActionEvent event) implements DataContext {
    @Override
    public Optional<Object> getData(String key) {
        return context.getData(key);
    }

    @Override
    public <T> Optional<T> getData(DataKey<T> key) {
        return context.getData(key);
    }

    ActionContext withEvent(ActionEvent event) {
        return new ActionContext(context, source, event);
    }
}
