package sh.adelessfox.odradek.ui;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.rtti.TypeInfo;

import javax.swing.*;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Preview<T> {
    static Stream<Preview<?>> previews() {
        class Holder {
            static final List<Preview<?>> previews = ServiceLoader.load(Preview.class).stream()
                .map(x -> (Preview<?>) x.get())
                .collect(Collectors.toUnmodifiableList());
        }
        return Holder.previews.stream();
    }

    @SuppressWarnings("unchecked")
    static <T> Optional<Preview<T>> preview(TypeInfo info) {
        return previews()
            .filter(p -> p.supports(info))
            .map(p -> (Preview<T>) p)
            .findFirst();
    }

    JComponent createComponent(T object, Game game);

    boolean supports(TypeInfo info);
}
