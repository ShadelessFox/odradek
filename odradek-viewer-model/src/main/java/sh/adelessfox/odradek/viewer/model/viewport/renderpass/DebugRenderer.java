package sh.adelessfox.odradek.viewer.model.viewport.renderpass;

import com.google.gson.Gson;
import org.lwjgl.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.geometry.Type;
import sh.adelessfox.odradek.math.*;
import sh.adelessfox.odradek.opengl.*;
import sh.adelessfox.odradek.rhi.AddressMode;
import sh.adelessfox.odradek.rhi.FilterMode;
import sh.adelessfox.odradek.rhi.SamplerDescriptor;
import sh.adelessfox.odradek.viewer.model.viewport.Camera;
import sh.adelessfox.odradek.viewer.model.viewport.Viewport;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL32.GL_PROGRAM_POINT_SIZE;
import static org.lwjgl.opengl.GL44.GL_DYNAMIC_STORAGE_BIT;

final class DebugRenderer {
    private static final Logger log = LoggerFactory.getLogger(DebugRenderer.class);

    private static final int MAX_LINES = 10000;
    private static final int MAX_POINTS = 10000;
    private static final int MAX_TEXTS = 1000;

    private static final int VERTEX_BUFFER_SIZE = 16384;
    private static final int VERTEX_SIZE = 7;

    private final FloatBuffer buffer = BufferUtils.createFloatBuffer(VERTEX_BUFFER_SIZE * VERTEX_SIZE);
    private final List<Line> lines = new ArrayList<>(MAX_LINES);
    private final List<Point> points = new ArrayList<>(MAX_POINTS);
    private final List<Text> texts = new ArrayList<>(MAX_TEXTS);
    private final Map<Integer, Glyph> glyphs = new HashMap<>();

    private final Font msdfFont;
    private final Texture msdfTexture;
    private final Sampler msdfSampler;

    private final ShaderProgram debugProgram;
    private final ShaderProgram msdfProgram;
    private final VertexArray vao;
    private final Buffer vbo;

    public DebugRenderer() throws IOException {
        debugProgram = ShaderProgram.ofVertexFragment(
            ShaderSource.read(getClass().getResource("/assets/shaders/debug.vert")),
            ShaderSource.read(getClass().getResource("/assets/shaders/debug.frag"))
        );
        msdfProgram = ShaderProgram.ofVertexFragment(
            ShaderSource.read(getClass().getResource("/assets/shaders/msdf.vert")),
            ShaderSource.read(getClass().getResource("/assets/shaders/msdf.frag"))
        );

        msdfFont = loadFontMetadata();
        msdfTexture = Texture.load(loadFontImage());
        msdfSampler = msdfTexture.createSampler(new SamplerDescriptor(
            AddressMode.CLAMP_TO_EDGE,
            AddressMode.CLAMP_TO_EDGE,
            FilterMode.LINEAR,
            FilterMode.LINEAR
        ));
        for (Glyph glyph : msdfFont.glyphs()) {
            glyphs.put(glyph.unicode(), glyph);
        }

        try (var _ = vao = new VertexArray().bind()) {
            var attributes = List.of(
                new VertexAttribute(0, new Type.F32(4), 0, 28),
                new VertexAttribute(1, new Type.F32(3), 16, 28)
            );

            vbo = vao.createVertexBuffer(attributes, 0);
            vbo.allocate(buffer.capacity() * Float.BYTES, GL_DYNAMIC_STORAGE_BIT);
        }

        // Required since the debug shader uses gl_PointSize
        glEnable(GL_PROGRAM_POINT_SIZE);
    }

    public void dispose() {
        debugProgram.dispose();
        msdfProgram.dispose();
        msdfSampler.dispose();
        msdfTexture.dispose();
        vao.dispose();
    }

    public void draw(Viewport viewport, double dt) {
        var camera = viewport.getCamera();
        if (camera == null) {
            return;
        }

        var depthTest = GLCapability.DEPTH_TEST.save();

        if (!lines.isEmpty() || !points.isEmpty()) {
            try (var _ = debugProgram.bind()) {
                debugProgram.set("u_view", camera.view());
                debugProgram.set("u_projection", camera.projection());

                drawLines();
                drawPoints();
            }
        }
        if (!texts.isEmpty()) {
            try (var _ = msdfProgram.bind()) {
                int width = viewport.getFramebufferWidth();
                int height = viewport.getFramebufferHeight();

                msdfProgram.set("u_transform", Matrix4f.ortho2D(0, width, height, 0));
                msdfProgram.set("u_msdf", msdfSampler);
                msdfProgram.set("u_distance_range", (float) msdfFont.atlas().distanceRange() / msdfFont.atlas().width());

                drawTexts(width, height);
            }
        }

        depthTest.restore();
    }

    public void point(Vector3f position, Vector3f color, float size) {
        point(position, color, size, true);
    }

    public void point(Vector3f position, Vector3f color, float size, boolean depthTest) {
        point(position.x(), position.y(), position.z(), color.x(), color.y(), color.z(), size, depthTest);
    }

    public void point(float x, float y, float z, float r, float g, float b, float size, boolean depthTest) {
        if (points.size() >= MAX_POINTS) {
            log.warn("Max points reached, skipping further point draws");
            return;
        }

        points.add(new Point(x, y, z, r, g, b, size, depthTest));
    }

    public void line(Vector3f from, Vector3f to, Vector3f color) {
        line(from, to, color, true);
    }

    public void line(Vector3f from, Vector3f to, Vector3f color, boolean depthTest) {
        line(from.x(), from.y(), from.z(), to.x(), to.y(), to.z(), color.x(), color.y(), color.z(), depthTest);
    }

    public void line(float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, boolean depthTest) {
        if (lines.size() >= MAX_LINES) {
            log.warn("Max lines reached, skipping further line draws");
            return;
        }

        lines.add(new Line(x1, y1, z1, x2, y2, z2, r, g, b, depthTest));
    }

    public void box(Vector3f[] points, Vector3f color) {
        box(points, color, true);
    }

    public void box(Vector3f[] points, Vector3f color, boolean depthTest) {
        for (int i = 0; i < 4; i++) {
            line(points[i], points[(i + 1) % 4], color, depthTest);
            line(points[i + 4], points[(i + 1) % 4 + 4], color, depthTest);
            line(points[i], points[i + 4], color, depthTest);
        }
    }

    public void aabb(BoundingBox bbox, Vector3f color) {
        aabb(bbox.min(), bbox.max(), color, true);
    }

    public void aabb(Vector3f min, Vector3f max, Vector3f color) {
        aabb(min, max, color, true);
    }

    public void aabb(Vector3f min, Vector3f max, Vector3f color, boolean depthTest) {
        final Vector3f[] points = {
            new Vector3f(min.x(), min.y(), min.z()),
            new Vector3f(max.x(), min.y(), min.z()),
            new Vector3f(max.x(), max.y(), min.z()),
            new Vector3f(min.x(), max.y(), min.z()),
            new Vector3f(min.x(), min.y(), max.z()),
            new Vector3f(max.x(), min.y(), max.z()),
            new Vector3f(max.x(), max.y(), max.z()),
            new Vector3f(min.x(), max.y(), max.z()),
        };

        box(points, color, depthTest);
    }

    public void cross(Vector3f center, float length) {
        cross(center, length, true);
    }

    public void cross(Vector3f center, float length, boolean depthTest) {
        float cx = center.x();
        float cy = center.y();
        float cz = center.z();
        float hl = length * 0.5f;

        line(cx - hl, cy, cz, cx + hl, cy, cz, 1, 0, 0, depthTest);
        line(cx, cy - hl, cz, cx, cy + hl, cz, 0, 1, 0, depthTest);
        line(cx, cy, cz - hl, cx, cy, cz + hl, 0, 0, 1, depthTest);
    }

    public void projectedText(String text, Vector3f position, Camera camera, Vector3f color, float scale) {
        projectedText(text, position.x(), position.y(), position.z(), camera.projectionView(), color.x(), color.y(), color.z(), scale);
    }

    public void projectedText(String text, float x, float y, float z, Matrix4f projection, float r, float g, float b, float scale) {
        Vector4f clip = new Vector4f(x, y, z, 1.0f).transform(projection);

        // Skip if the point is behind the camera or outside the screen
        if (Math.abs(clip.w()) < 1e-6 || clip.z() < -clip.w() || clip.z() > clip.w()) {
            return;
        }

        float nx = (clip.x() / clip.w() * 0.5f) + 0.5f;
        float ny = ((-(clip.y() / clip.w()) * 0.5f) + 0.5f);

        text(text, nx, ny, r, g, b, scale, true, false);
    }

    public void billboardText(String text, Vector2f position, Vector3f color, float scale) {
        billboardText(text, position.x(), position.y(), color.x(), color.y(), color.z(), scale);
    }

    public void billboardText(String text, float x, float y, float r, float g, float b, float scale) {
        text(text, x, y, r, g, b, scale, false, false);
    }

    private void text(String text, float x, float y, float r, float g, float b, float scale, boolean normalized, boolean depthTest) {
        if (texts.size() >= MAX_TEXTS) {
            log.warn("Max texts reached, skipping further text draws");
            return;
        }

        texts.add(new Text(text, x, y, r, g, b, scale, normalized, depthTest));
    }

    private void drawPoints() {
        drawPoints(false);
        drawPoints(true);
        points.clear();
    }

    private void drawPoints(boolean depthTest) {
        for (Point point : points) {
            if (point.depthTest == depthTest) {
                push(point, depthTest);
            }
        }

        flush(GL_POINTS, depthTest);
    }

    private void drawLines() {
        drawLines(false);
        drawLines(true);
        lines.clear();
    }

    private void drawLines(boolean depthTest) {
        for (Line line : lines) {
            if (line.depthTest == depthTest) {
                push(line, depthTest);
            }
        }

        flush(GL_LINES, depthTest);
    }

    private void drawTexts(int width, int height) {
        drawTexts(width, height, false);
        drawTexts(width, height, true);
        texts.clear();
    }

    private void drawTexts(int width, int height, boolean depthTest) {
        for (Text text : texts) {
            if (text.depthTest == depthTest) {
                push(text, width, height, depthTest);
            }
        }

        flush(GL_TRIANGLES, depthTest);
    }

    private void flush(int mode, boolean depthTest) {
        GLCapability.DEPTH_TEST.set(depthTest);

        int remaining = buffer.position() % VERTEX_SIZE;
        if (remaining != 0) {
            throw new IllegalStateException("buffer is expected to only contain entries of VERTEX_SIZE size");
        }

        int count = buffer.position() / VERTEX_SIZE;
        if (count == 0) {
            return;
        }

        try (var _ = vao.bind()) {
            vbo.update(buffer.flip(), 0);
            glDrawArrays(mode, 0, count);
        }

        buffer.clear();
    }

    private void push(Point value, boolean depthTest) {
        if (buffer.remaining() < VERTEX_SIZE) {
            flush(GL_POINTS, depthTest);
        }

        push(value.x, value.y, value.z, value.size, value.r, value.g, value.b);
    }

    private void push(Line value, boolean depthTest) {
        if (buffer.remaining() < VERTEX_SIZE * 2) {
            flush(GL_LINES, depthTest);
        }

        push(value.x1, value.y1, value.z1, 0.0f, value.c1, value.c2, value.c3);
        push(value.x2, value.y2, value.z2, 0.0f, value.c1, value.c2, value.c3);
    }

    private void push(Text text, int width, int height, boolean depthTest) {
        int length = text.text.length();

        float x = text.x;
        float y = text.y;

        if (text.normalized) {
            x *= width;
            y *= height;
        }

        float originalX = x;

        for (int i = 0; i < length; i++) {
            var codePoint = text.text.codePointAt(i);
            if (codePoint == '\n') {
                y += msdfFont.metrics.lineHeight * text.scale;
                x = originalX;
                continue;
            }

            var glyph = glyphs.get(codePoint);
            if (glyph == null) {
                continue;
            }

            if (glyph.planeBounds != null) {
                // Border takes up approximately 10% of the text's scale.
                float x1 = x + (glyph.planeBounds.left + 0.1f) * text.scale;
                float y1 = y + (glyph.planeBounds.top + 0.9f) * text.scale;
                float x2 = x + (glyph.planeBounds.right + 0.1f) * text.scale;
                float y2 = y + (glyph.planeBounds.bottom + 0.9f) * text.scale;

                float u1 = glyph.atlasBounds.left / msdfFont.atlas.width;
                float v1 = glyph.atlasBounds.top / msdfFont.atlas.height;
                float u2 = glyph.atlasBounds.right / msdfFont.atlas.width;
                float v2 = glyph.atlasBounds.bottom / msdfFont.atlas.height;

                if (buffer.remaining() < VERTEX_SIZE * 6) {
                    flush(GL_TRIANGLES, depthTest);
                }

                push(x1, y1, u1, v1, text.r, text.g, text.b);
                push(x2, y2, u2, v2, text.r, text.g, text.b);
                push(x2, y1, u2, v1, text.r, text.g, text.b);

                push(x1, y1, u1, v1, text.r, text.g, text.b);
                push(x1, y2, u1, v2, text.r, text.g, text.b);
                push(x2, y2, u2, v2, text.r, text.g, text.b);
            }

            x += glyph.advance * text.scale * 1.1f;
        }
    }

    private void push(float x, float y, float z, float w, float r, float g, float b) {
        buffer.put(x).put(y).put(z).put(w);
        buffer.put(r).put(g).put(b);
    }

    private BufferedImage loadFontImage() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/assets/textures/font.png")) {
            if (is == null) {
                throw new IOException("Font image not found");
            }
            return ImageIO.read(is);
        }
    }

    private Font loadFontMetadata() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/assets/textures/font.json")) {
            if (is == null) {
                throw new IOException("Font metadata not found");
            }
            return new Gson().fromJson(new InputStreamReader(is), Font.class);
        }
    }

    private record Line(
        float x1, float y1, float z1,
        float x2, float y2, float z2,
        float c1, float c2, float c3,
        boolean depthTest
    ) {
    }

    private record Point(float x, float y, float z, float r, float g, float b, float size, boolean depthTest) {
    }

    private record Text(String text, float x, float y, float r, float g, float b, float scale, boolean normalized,
                        boolean depthTest) {
    }

    private record Font(Atlas atlas, Metrics metrics, Glyph[] glyphs) {
    }

    private record Atlas(int distanceRange, int width, int height) {
    }

    private record Metrics(float lineHeight, float ascender, float descender) {
    }

    private record Glyph(int unicode, float advance, Bounds planeBounds, Bounds atlasBounds) {
    }

    private record Bounds(float left, float top, float right, float bottom) {
    }
}
