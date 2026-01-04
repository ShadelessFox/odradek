package sh.adelessfox.odradek.ui.editors;

import sh.adelessfox.odradek.ui.Activable;
import sh.adelessfox.odradek.ui.Disposable;
import sh.adelessfox.odradek.ui.Focusable;

import javax.swing.*;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public interface Editor extends Activable, Focusable, Disposable {
    interface Provider {
        Editor createEditor(EditorInput input, EditorSite site);

        Match matches(EditorInput input);

        enum Match {
            /** The provider is determined to handle the supplied input */
            PRIMARY,
            /** The provider can handle the supplied input, but only if no providers with {@link Match#PRIMARY} was found */
            APPLIES,
            /** The provider can't handle the supplied input */
            NONE
        }
    }

    static Stream<Provider> providers() {
        class Holder {
            static final List<Provider> providers = ServiceLoader.load(Provider.class).stream()
                .map(ServiceLoader.Provider::get)
                .toList();
        }
        return Holder.providers.stream();
    }

    static Stream<Provider> providers(EditorInput input) {
        return providers()
            .filter(provider -> provider.matches(input) != Provider.Match.NONE)
            .sorted(Comparator.comparing(provider -> provider.matches(input)));
    }

    JComponent createComponent();

    EditorInput getInput();

    @Override
    default void activate() {
        // do nothing by default
    }

    @Override
    default void deactivate() {
        // do nothing by default
    }

    @Override
    default void dispose() {
        // do nothing by default
    }
}
