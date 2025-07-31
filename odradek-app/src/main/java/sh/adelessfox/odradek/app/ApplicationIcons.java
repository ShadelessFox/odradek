package sh.adelessfox.odradek.app;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import java.util.Objects;

public final class ApplicationIcons {
    public static final FlatSVGIcon CASE_SENSITIVE = load("/icons/case-sensitive.svg");
    public static final FlatSVGIcon WHOLE_WORD = load("/icons/whole-word.svg");

    private ApplicationIcons() {
    }

    private static FlatSVGIcon load(String path) {
        return new FlatSVGIcon(Objects.requireNonNull(ApplicationIcons.class.getResource(path), path));
    }
}
