package sh.adelessfox.odradek.ui.data;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

@FunctionalInterface
public interface DataContext {
    /**
     * Returns a {@link DataContext} that retrieves data from the current focused component.
     */
    static DataContext focusedComponent() {
        return key -> {
            var manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            for (Component c = manager.getPermanentFocusOwner(); c != null; c = c.getParent()) {
                var result = getDataContext(c).flatMap(context -> context.getData(key));
                if (result.isPresent()) {
                    return result;
                }
            }
            return Optional.empty();
        };
    }

    /**
     * Returns an empty {@link DataContext}.
     */
    static DataContext empty() {
        return _ -> Optional.empty();
    }

    static Optional<DataContext> getDataContext(Component component) {
        return switch (component) {
            case DataContext context -> Optional.of(context);
            case JComponent c when c.getClientProperty(DataContext.class) instanceof DataContext context -> Optional.of(context);
            case null, default -> Optional.empty();
        };
    }

    static void putDataContext(JComponent component, DataContext context) {
        component.putClientProperty(DataContext.class, context);
    }

    Optional<Object> getData(String key);

    @SuppressWarnings("unchecked")
    default <T> Optional<T> getData(DataKey<T> key) {
        return getData(key.name()).map(x -> (T) x);
    }
}
