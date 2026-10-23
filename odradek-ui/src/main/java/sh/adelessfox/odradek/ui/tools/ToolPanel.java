package sh.adelessfox.odradek.ui.tools;

import javax.swing.*;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public interface ToolPanel {
    /** Stateless metadata and factory; dependencies are supplied only when a panel is created. */
    interface Provider {
        ToolPanel create(ToolSite site);

        String id();

        String name();

        Icon icon();

        /** Initial placement, before applying the user's saved layout. */
        Placement placement();

        /** Registration order; equal orders are sorted by panel ID. */
        default int order() {
            return 0;
        }
    }

    record Placement(Anchor anchor, boolean primary) {
        public enum Anchor {
            LEFT,
            RIGHT,
            BOTTOM
        }
    }

    static Stream<Provider> providers() {
        class Holder {
            static final List<Provider> providers = ServiceLoader.load(Provider.class).stream()
                .map(ServiceLoader.Provider::get)
                .sorted(Comparator.comparingInt(Provider::order).thenComparing(Provider::id))
                .toList();
        }
        return Holder.providers.stream();
    }

    JComponent createComponent();
}
