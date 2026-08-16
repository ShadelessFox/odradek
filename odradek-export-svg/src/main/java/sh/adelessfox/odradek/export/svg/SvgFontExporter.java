package sh.adelessfox.odradek.export.svg;

import org.w3c.dom.Document;
import sh.adelessfox.odradek.font.Font;
import sh.adelessfox.odradek.font.Glyph;
import sh.adelessfox.odradek.game.Exporter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Optional;

public final class SvgFontExporter implements Exporter.OfSingleOutput<Font> {
    @Override
    public void export(Font object, WritableByteChannel channel) throws IOException {
        Document document;
        try {
            var documentBuilderFactory = DocumentBuilderFactory.newInstance();
            var documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.newDocument();
        } catch (Exception e) {
            throw new IOException(e);
        }

        var elemSvg = document.createElement("svg");
        document.appendChild(elemSvg);
        elemSvg.setAttribute("version", "1.1");
        elemSvg.setAttribute("width", "100%");
        elemSvg.setAttribute("height", "100%");

        var elemDefs = document.createElement("defs");
        elemSvg.appendChild(elemDefs);

        var elemFont = document.createElement("font");
        elemSvg.appendChild(elemFont);

        var elemFontFace = document.createElement("font-face");
        elemFont.appendChild(elemFontFace);
        elemFontFace.setAttribute("font-family", object.name());
        elemFontFace.setAttribute("units-per-em", String.valueOf(object.metrics().height()));
        elemFontFace.setAttribute("ascent", String.valueOf(object.metrics().ascent()));
        elemFontFace.setAttribute("descent", String.valueOf(object.metrics().descent()));
        elemFontFace.setAttribute("cap-height", String.valueOf(object.metrics().emHeight()));
        elemFontFace.setAttribute("vert-origin-y", "0");

        for (Glyph glyph : object.glyphs()) {
            var elemGlyph = document.createElement("glyph");
            elemFont.appendChild(elemGlyph);
            elemGlyph.setAttribute("unicode", Character.toString(glyph.codePoint()));
            elemGlyph.setAttribute("horiz-adv-x", String.valueOf(glyph.metrics().advanceWidth()));

            var elemPath = document.createElement("path");
            elemGlyph.appendChild(elemPath);
            elemPath.setAttribute("d", toSvgPath(glyph, object.metrics().ascent()));
        }

        try (OutputStream os = Channels.newOutputStream(channel)) {
            var transformerFactory = TransformerFactory.newInstance();
            var transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(document), new StreamResult(os));
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public String extension() {
        return "svg";
    }

    @Override
    public String id() {
        return "font.svg";
    }

    @Override
    public String name() {
        return "SVG Font";
    }

    @Override
    public Optional<String> icon() {
        return Optional.of("fugue:edit");
    }

    private static String toSvgPath(Glyph glyph, float ascent) {
        return toSvgPath(glyph.toPath(), ascent);
    }

    private static String toSvgPath(Path2D path, float ascent) {
        var sb = new StringBuilder();
        var it = path.getPathIterator(null);
        var coords = new float[6];

        while (!it.isDone()) {
            switch (it.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO -> sb.append("M%s,%s".formatted(
                    coords[0], ascent - coords[1]));
                case PathIterator.SEG_LINETO -> sb.append("L%s,%s".formatted(
                    coords[0], ascent - coords[1]));
                case PathIterator.SEG_CUBICTO -> sb.append("C%s,%s,%s,%s,%s,%s".formatted(
                    coords[0], ascent - coords[1],
                    coords[2], ascent - coords[3],
                    coords[4], ascent - coords[5]));
                case PathIterator.SEG_CLOSE -> sb.append('Z');
            }

            it.next();
        }

        return sb.toString();
    }
}
