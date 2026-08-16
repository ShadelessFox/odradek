package sh.adelessfox.odradek.font;

import java.util.List;

public record Font(
    String name,
    Metrics metrics,
    List<Glyph> glyphs
) {
    public record Metrics(float height, float ascent, float descent, float emHeight) {
    }

    public Font {
        glyphs = List.copyOf(glyphs);
    }
}
