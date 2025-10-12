package sh.adelessfox.odradek.ui.editors;

import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public interface EditorProvider {
    static Stream<EditorProvider> providers() {
        class Holder {
            static final List<EditorProvider> providers = ServiceLoader.load(EditorProvider.class).stream()
                .map(ServiceLoader.Provider::get)
                .toList();
        }
        return Holder.providers.stream();
    }

    static Stream<EditorProvider> providers(EditorInput input) {
        return providers()
            .filter(provider -> provider.matches(input) != Match.NONE)
            .sorted(Comparator.comparing(provider -> provider.matches(input)));
    }

    Editor createEditor(EditorInput input);

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
