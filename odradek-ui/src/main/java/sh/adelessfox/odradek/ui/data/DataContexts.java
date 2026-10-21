package sh.adelessfox.odradek.ui.data;

import sh.adelessfox.odradek.data.DataContext;
import sh.adelessfox.odradek.data.DataContextOwner;
import sh.adelessfox.odradek.data.DataKey;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/** Swing attachment and component-hierarchy lookup for data contexts. */
public final class DataContexts {
    private DataContexts() {
    }

    public static Optional<DataContext> getDataContext(Component component) {
        return switch (component) {
            case JComponent c when c.getClientProperty(DataContext.class) instanceof DataContext context -> Optional.of(context);
            case DataContextOwner owner -> Optional.of(owner.getDataContext());
            case null, default -> Optional.empty();
        };
    }

    public static void putDataContext(JComponent component, DataContext context) {
        component.putClientProperty(DataContext.class, Objects.requireNonNull(context, "context"));
    }

    public static DataContext focusedComponent() {
        return new ComponentContext(() -> {
            var manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            var focusOwner = manager.getPermanentFocusOwner();
            return focusOwner != null ? focusOwner : manager.getFocusOwner();
        });
    }

    public static DataContext forComponent(Component component) {
        Objects.requireNonNull(component, "component");
        return new ComponentContext(() -> component);
    }

    private static final class ComponentContext extends DataContext {
        private final Supplier<Component> component;

        private ComponentContext(Supplier<Component> component) {
            this.component = component;
        }

        @Override
        protected <T> Lookup<T> lookup(DataKey<T> key) {
            var local = super.lookup(key);
            if (!local.isMissing()) {
                return local;
            }
            for (Component c = component.get(); c != null; c = c.getParent()) {
                var context = getDataContext(c);
                if (context.isPresent()) {
                    var result = lookupIn(context.get(), key);
                    if (!result.isMissing()) {
                        return result;
                    }
                }
            }
            return Lookup.missing();
        }
    }
}
