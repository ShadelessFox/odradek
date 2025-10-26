package sh.adelessfox.odradek.ui.util;

import javax.swing.*;
import java.net.URI;
import java.util.Optional;

public final class Icons {
    private Icons() {
    }

    /**
     * Retrieves an icon from the given uri.
     * <p>
     * Supports the following schemes:
     * <ul>
     *     <li>{@code fugue:{fugue-icon-name}}</li>
     * </ul>
     *
     * @param string the icon's uri
     * @return the icon, or {@link Optional#empty()} if it can't be found
     * @throws IllegalArgumentException if invalid scheme is supplied
     */
    public static Optional<Icon> getIconFromUri(String string) {
        if (string.isEmpty()) {
            return Optional.empty();
        }
        URI uri = URI.create(string);
        return switch (uri.getScheme()) {
            case "fugue" -> Optional.of(Fugue.getIcon(uri.getSchemeSpecificPart()));
            default -> throw new IllegalArgumentException("Invalid scheme: " + uri);
        };
    }
}
