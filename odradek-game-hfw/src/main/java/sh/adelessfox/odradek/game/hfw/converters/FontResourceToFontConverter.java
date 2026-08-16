package sh.adelessfox.odradek.game.hfw.converters;

import sh.adelessfox.odradek.font.Font;
import sh.adelessfox.odradek.font.Glyph;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.HFWGame;
import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import wtf.reversed.toolbox.math.Vector2;

import java.util.Optional;

public final class FontResourceToFontConverter implements Converter<HFW.FontResource, Font, HFWGame> {
    @Override
    public Optional<Font> convert(HFW.FontResource object, HFWGame game) {
        var data = object.fontResourceData();
        var name = data.typefaceName();
        var metrics = convertFontMetrics(data.textMetrics());
        var glyphs = data.codePointInfo().stream()
            .map(FontResourceToFontConverter::convertGlyph)
            .toList();

        return Optional.of(new Font(name, metrics, glyphs));
    }

    private static Font.Metrics convertFontMetrics(HFW.FontTextMetrics metrics) {
        return new Font.Metrics(
            metrics.height(),
            metrics.ascent(),
            metrics.descent(),
            metrics.emHeight());
    }

    private static Glyph convertGlyph(HFW.FontCodePointGlyphInfo info) {
        int codePoint = info.codePoint();
        var metrics = convertGlyphMetrics(info);
        var bounds = convertGlyphBounds(info);
        var contours = info.glyphContourList().glyphContours().stream()
            .map(FontResourceToFontConverter::convertGlyphContour)
            .toList();

        return new Glyph(codePoint, metrics, bounds, contours);
    }

    private static Glyph.Contour convertGlyphContour(HFW.GlyphContour contour) {
        var points = contour.points().stream()
            .map(FontResourceToFontConverter::convertGlyphPoint)
            .toList();
        var commands = contour.commandList().stream()
            .map(FontResourceToFontConverter::convertGlyphCommand)
            .toList();

        return new Glyph.Contour(commands, points);
    }

    private static Vector2 convertGlyphPoint(HFW.Vec2Pack point) {
        return new Vector2(point.x(), point.y());
    }

    private static Glyph.Command convertGlyphCommand(HFW.GlyphContourCmd command) {
        var data = command.cmdData() & 0xff;
        var curve = (data & 64) != 0;
        var count = (data & 63);

        if (curve) {
            return new Glyph.Command.CurveTo(count);
        } else {
            return new Glyph.Command.LineTo(count);
        }
    }

    private static Glyph.Metrics convertGlyphMetrics(HFW.FontCodePointGlyphInfo info) {
        var advanceWidth = info.glyphMetrics().advanceWidth();
        return new Glyph.Metrics(advanceWidth);
    }

    private static Glyph.Bounds convertGlyphBounds(HFW.FontCodePointGlyphInfo info) {
        var bounds = info.glyphContourList().bounds();
        return new Glyph.Bounds(
            bounds.min().x(),
            bounds.min().y(),
            bounds.max().x(),
            bounds.max().y());
    }
}
