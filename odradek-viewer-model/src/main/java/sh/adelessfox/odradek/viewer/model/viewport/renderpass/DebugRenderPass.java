package sh.adelessfox.odradek.viewer.model.viewport.renderpass;

import com.google.gson.Gson;
import org.lwjgl.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.geometry.ComponentType;
import sh.adelessfox.odradek.geometry.ElementType;
import sh.adelessfox.odradek.math.Matrix4f;
import sh.adelessfox.odradek.math.Vector2f;
import sh.adelessfox.odradek.math.Vector3f;
import sh.adelessfox.odradek.math.Vector4f;
import sh.adelessfox.odradek.opengl.*;
import sh.adelessfox.odradek.viewer.model.viewport.Camera;
import sh.adelessfox.odradek.viewer.model.viewport.Viewport;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL32.GL_PROGRAM_POINT_SIZE;

class DebugRenderPass implements RenderPass {
    private static final Logger log = LoggerFactory.getLogger(DebugRenderPass.class);

    private static final int MAX_LINES = 1000;
    private static final int MAX_POINTS = 1000;
    private static final int MAX_TEXTS = 1000;

    private static final int VERTEX_BUFFER_SIZE = 16384;
    private static final int VERTEX_SIZE = 7;

    private final FloatBuffer buffer = BufferUtils.createFloatBuffer(VERTEX_BUFFER_SIZE * VERTEX_SIZE);
    private final List<Line> lines = new ArrayList<>(MAX_LINES);
    private final List<Point> points = new ArrayList<>(MAX_POINTS);
    private final List<Text> texts = new ArrayList<>(MAX_TEXTS);
    private final Map<Integer, Glyph> glyphs = new HashMap<>();

    private Atlas msdfAtlas;

    private ShaderProgram debugProgram;
    private ShaderProgram msdfProgram;
    private Texture msdfTexture;
    private VertexArray vao;
    private VertexBuffer vbo;

    @Override
    public void init() {
        try {
            debugProgram = new ShaderProgram(
                ShaderSource.fromResource(DebugRenderPass.class.getResource("/assets/shaders/debug.vert")),
                ShaderSource.fromResource(DebugRenderPass.class.getResource("/assets/shaders/debug.frag"))
            );
            msdfProgram = new ShaderProgram(
                ShaderSource.fromResource(DebugRenderPass.class.getResource("/assets/shaders/msdf.vert")),
                ShaderSource.fromResource(DebugRenderPass.class.getResource("/assets/shaders/msdf.frag"))
            );
        } catch (IOException e) {
            log.error("Failed to load shaders", e);
        }

        try (var _ = msdfTexture = new Texture().bind()) {
            var metadata = loadFontMetadata();
            msdfTexture.put(loadFontImage());
            msdfAtlas = metadata.atlas();
            for (Glyph glyph : metadata.glyphs()) {
                glyphs.put(glyph.unicode(), glyph);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load font", e);
        }

        try (var _ = vao = new VertexArray().bind()) {
            var attributes = List.of(
                new VertexAttribute(0, ElementType.VEC4, ComponentType.FLOAT, 0, 28, false),
                new VertexAttribute(1, ElementType.VEC3, ComponentType.FLOAT, 16, 28, false)
            );

            try (var _ = vbo = vao.createBuffer(attributes)) {
                vbo.allocate(buffer.capacity() * Float.BYTES, GL_STREAM_DRAW);
            }
        }

        // Required since the debug shader uses gl_PointSize
        glEnable(GL_PROGRAM_POINT_SIZE);
    }

    @Override
    public void dispose() {
        debugProgram.dispose();
        msdfProgram.dispose();
        msdfTexture.dispose();
        vao.dispose();
    }

    @Override
    public void draw(Viewport viewport, double dt) {
        Camera camera = viewport.getCamera();
        if ((!lines.isEmpty() || !points.isEmpty()) && camera != null) {
            try (var _ = debugProgram.bind()) {
                debugProgram.set("u_view", camera.view());
                debugProgram.set("u_projection", camera.projection());

                drawLines();
                drawPoints();
            }
        }
        if (!texts.isEmpty()) {
            try (var _ = msdfProgram.bind(); var _ = msdfTexture.bind()) {
                int width = viewport.getFramebufferWidth();
                int height = viewport.getFramebufferHeight();

                msdfProgram.set("u_transform", Matrix4f.ortho2D(0, width, height, 0));
                msdfProgram.set("u_msdf", 0);
                msdfProgram.set("u_distance_range", (float) msdfAtlas.distanceRange / msdfAtlas.width);

                drawTexts(width, height);
            }
        }
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

    public void box(Vector3f[] points, Vector3f color, boolean depthTest) {
        for (int i = 0; i < 4; i++) {
            line(points[i], points[(i + 1) % 4], color, depthTest);
            line(points[i + 4], points[(i + 1) % 4 + 4], color, depthTest);
            line(points[i], points[i + 4], color, depthTest);
        }
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

    public void cross(Vector3f center, float length, boolean depthTest) {
        float cx = center.x();
        float cy = center.y();
        float cz = center.z();
        float hl = length * 0.5f;

        line(cx - hl, cy, cz, cx + hl, cy, cz, 1, 0, 0, depthTest);
        line(cx, cy - hl, cz, cx, cy + hl, cz, 0, 1, 0, depthTest);
        line(cx, cy, cz - hl, cx, cy, cz + hl, 0, 0, 1, depthTest);
    }

    public void projectedText(String text, Vector3f position, Matrix4f transform, Vector3f color, float scale) {
        projectedText(text, position.x(), position.y(), position.z(), transform, color.x(), color.y(), color.z(), scale);
    }

    public void projectedText(String text, float x, float y, float z, Matrix4f projection, float r, float g, float b, float scale) {
        Vector4f clip = new Vector4f(x, y, z, 1.0f).mul(projection);

        // Skip if the point is behind the camera or outside the screen
        if (Math.abs(clip.w()) < 1e-6 || clip.z() < -clip.w() || clip.z() > clip.w()) {
            return;
        }

        float nx = (clip.x() / clip.w() * 0.5f) + 0.5f;
        float ny = ((-(clip.y() / clip.w()) * 0.5f) + 0.5f);

        text(text, nx, ny, r, g, b, scale, true);
    }

    public void billboardText(String text, Vector2f position, Vector3f color, float scale) {
        billboardText(text, position.x(), position.y(), color.x(), color.y(), color.z(), scale);
    }

    public void billboardText(String text, float x, float y, float r, float g, float b, float scale) {
        text(text, x, y, r, g, b, scale, false);
    }

    private void text(String text, float x, float y, float r, float g, float b, float scale, boolean normalized) {
        if (texts.size() >= MAX_TEXTS) {
            log.warn("Max texts reached, skipping further text draws");
            return;
        }

        texts.add(new Text(text, x, y, r, g, b, scale, normalized));
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
        texts.clear();
    }

    private void drawTexts(int width, int height, boolean depthTest) {
        for (Text text : texts) {
            push(text, width, height, depthTest);
        }

        flush(GL_TRIANGLES, depthTest);
    }

    private void flush(int mode, boolean depthTest) {
        if (depthTest) {
            glEnable(GL_DEPTH_TEST);
        } else {
            glDisable(GL_DEPTH_TEST);
        }

        final int count = buffer.position() / VERTEX_SIZE;

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

        for (int i = 0; i < length; i++) {
            var codePoint = text.text.codePointAt(i);
            var glyph = glyphs.get(codePoint);

            if (glyph == null) {
                continue;
            }

            if (glyph.planeBounds != null) {
                float x1 = x + glyph.planeBounds.left * text.scale;
                float y1 = y + glyph.planeBounds.top * text.scale;
                float x2 = x + glyph.planeBounds.right * text.scale;
                float y2 = y + glyph.planeBounds.bottom * text.scale;

                float u1 = glyph.atlasBounds.left / msdfAtlas.width;
                float v1 = glyph.atlasBounds.top / msdfAtlas.height;
                float u2 = glyph.atlasBounds.right / msdfAtlas.width;
                float v2 = glyph.atlasBounds.bottom / msdfAtlas.height;

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

            x += glyph.advance * text.scale;
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

    private record Text(String text, float x, float y, float r, float g, float b, float scale, boolean normalized) {
    }

    private record Font(Atlas atlas, Glyph[] glyphs) {
    }

    private record Atlas(int distanceRange, int width, int height) {
    }

    private record Glyph(int unicode, float advance, Bounds planeBounds, Bounds atlasBounds) {
    }

    private record Bounds(float left, float top, float right, float bottom) {
    }
}
