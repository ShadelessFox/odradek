package sh.adelessfox.odradek.app.viewport.renderpass;

import com.google.gson.Gson;
import org.lwjgl.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.gl.*;
import sh.adelessfox.odradek.app.viewport.Viewport;
import sh.adelessfox.odradek.geometry.ComponentType;
import sh.adelessfox.odradek.geometry.ElementType;
import sh.adelessfox.odradek.math.Mat4;
import sh.adelessfox.odradek.math.Vec2;
import sh.adelessfox.odradek.math.Vec3;
import sh.adelessfox.odradek.math.Vec4;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;

public class TextRenderPass implements RenderPass {
    private static final Logger log = LoggerFactory.getLogger(TextRenderPass.class);

    private static final int MAX_TEXTS = 32768;
    private static final int VERTEX_BUFFER_SIZE = 4096;
    private static final int VERTEX_ELEMENTS = 7;
    private static final float FLOAT_EPSILON = Math.ulp(1.0f);

    private final FloatBuffer vertices = BufferUtils.createFloatBuffer(VERTEX_BUFFER_SIZE * VERTEX_ELEMENTS);
    private final Deque<Text> texts = new ArrayDeque<>(MAX_TEXTS);
    private final Map<Integer, Glyph> glyphs = new HashMap<>();

    // region Shaders
    private static final ShaderSource VERTEX_SHADER = new ShaderSource("main.vert", """
        #version 330 core
        
        in vec2 in_position;
        in vec2 in_uv;
        in vec3 in_color;
        
        out vec2 io_uv;
        out vec3 io_color;
        
        uniform mat4 u_transform;
        
        void main() {
            gl_Position = u_transform * vec4(in_position, 0.0, 1.0);
            io_uv = in_uv;
            io_color = in_color;
        }""");

    private static final ShaderSource FRAGMENT_SHADER = new ShaderSource("main.frag", """
        #version 330 core
        
        in vec2 io_uv;
        in vec3 io_color;
        
        out vec4 out_color;
        
        uniform sampler2D u_msdf;
        uniform float u_distance_range;
        
        float median(float r, float g, float b) {
            return max(min(r, g), min(max(r, g), b));
        }
        
        float screenPxRange() {
            vec2 unitRange = vec2(u_distance_range);
            vec2 screenTexSize = vec2(1.0) / fwidth(io_uv);
            return max(0.5 * dot(unitRange, screenTexSize), 1.0);
        }
        
        void main() {
            vec3 tex = texture(u_msdf, io_uv).rgb;
            float distance = median(tex.r, tex.g, tex.b);
            float range = screenPxRange();
        
            #define WEIGHT 1.0
            #define OUTLINE_OFFSET 0.7
            #define OUTLINE_COLOR vec3(0.0)
            #define BIAS 0.5
        
            // offset the SDF edge to make the font appear wider or slimmer
            float weight = (0.5 + (WEIGHT * -0.1));
            float characterDistance = range * (distance - weight);
            float outlineDistance = range * (distance - (weight - OUTLINE_OFFSET));
        
            // calculate how much color is required
            float characterColorAmount = clamp(characterDistance + 0.5, 0.0, 1.0);
            float outlineColorAmount = clamp(outlineDistance + 0.5, 0.0, 1.0);
        
            // mix between font color and outline color
            out_color.rgb = mix(OUTLINE_COLOR, io_color.rgb, characterColorAmount);
        
            // opacity is a max, so unreasonable outlines don't destroy the rendering
            out_color.a = pow(smoothstep(0.025, WEIGHT - OUTLINE_OFFSET, distance), BIAS);
        
            if (out_color.a < 0.01) {
                discard;
            }
        }""");
    // endregion

    private ShaderProgram program;
    private VertexArray vao;
    private VertexBuffer vbo;

    private int texture;
    private Font font;

    @Override
    public void init() {
        program = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);

        try (var _ = vao = new VertexArray().bind()) {
            var attributes = List.of(
                new VertexAttribute(0, ElementType.VEC2, ComponentType.FLOAT, 0, 28, false),
                new VertexAttribute(1, ElementType.VEC2, ComponentType.FLOAT, 8, 28, false),
                new VertexAttribute(2, ElementType.VEC3, ComponentType.FLOAT, 16, 28, false)
            );

            try (var _ = vbo = vao.createBuffer(attributes)) {
                vbo.allocate(vertices.capacity() * Float.BYTES, GL_STREAM_DRAW);
            }
        }

        try {
            texture = createTexture(loadFontImage());
            font = loadFontMetadata();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load font", e);
        }

        for (Glyph glyph : font.glyphs) {
            glyphs.put(glyph.unicode, glyph);
        }
    }

    @Override
    public void dispose() {
        program.dispose();
        vao.dispose();
        glDeleteTextures(texture);
    }

    @Override
    public void draw(Viewport viewport, double dt) {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture);

        int width = viewport.getFramebufferWidth();
        int height = viewport.getFramebufferHeight();

        try (var _ = program.bind()) {
            program.set("u_transform", Mat4.ortho2D(0, width, height, 0));
            program.set("u_msdf", 0);
            program.set("u_distance_range", (float) font.atlas.distanceRange / font.atlas.width);

            for (Text text : texts) {
                push(text, width, height);
            }
            texts.clear();
            flushVertices();
        }

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void projectedText(String text, Vec3 position, Mat4 transform, Vec3 color, float scale) {
        projectedText(text, position.x(), position.y(), position.z(), transform, color.x(), color.y(), color.z(), scale);
    }

    public void projectedText(String text, float x, float y, float z, Mat4 viewProjection, float r, float g, float b, float scale) {
        Vec4 clip = new Vec4(x, y, z, 1.0f).mul(viewProjection);

        // Skip if the point is behind the camera or outside the screen
        if (Math.abs(clip.w()) < FLOAT_EPSILON || clip.z() < -clip.w() || clip.z() > clip.w()) {
            return;
        }

        float nx = (clip.x() / clip.w() * 0.5f) + 0.5f;
        float ny = ((-(clip.y() / clip.w()) * 0.5f) + 0.5f);

        text(text, nx, ny, r, g, b, scale, true);
    }

    public void billboardText(String text, Vec2 position, Vec3 color, float scale) {
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

        texts.offer(new Text(text, x, y, r, g, b, scale, normalized));
    }

    private void push(Text text, int width, int height) {
        int length = text.text.length();

        float x = text.x;
        float y = text.y;

        if (text.normalized) {
            x *= width;
            y *= height;
        }

        for (int i = 0; i < length; i++) {
            int ch = text.text.codePointAt(i);
            Glyph glyph = glyphs.get(ch);

            if (glyph == null) {
                continue;
            }

            if (glyph.planeBounds != null) {
                float x1 = x + glyph.planeBounds.left * text.scale;
                float y1 = y + glyph.planeBounds.top * text.scale;
                float x2 = x + glyph.planeBounds.right * text.scale;
                float y2 = y + glyph.planeBounds.bottom * text.scale;

                float u1 = glyph.atlasBounds.left / font.atlas.width;
                float v1 = glyph.atlasBounds.top / font.atlas.height;
                float u2 = glyph.atlasBounds.right / font.atlas.width;
                float v2 = glyph.atlasBounds.bottom / font.atlas.height;

                push(x1, y1, x2, y2, x2, y1, u1, v1, u2, v2, u2, v1, text.r, text.g, text.b);
                push(x1, y1, x1, y2, x2, y2, u1, v1, u1, v2, u2, v2, text.r, text.g, text.b);
            }

            x += glyph.advance * text.scale;
        }
    }

    private void push(
        float x1, float y1, float x2, float y2, float x3, float y3,
        float u1, float v1, float u2, float v2, float u3, float v3,
        float r, float g, float b
    ) {
        if (vertices.remaining() < VERTEX_ELEMENTS * 3) {
            // Make room for the new triangle
            flushVertices();
        }

        push(x1, y1, u1, v1, r, g, b);
        push(x2, y2, u2, v2, r, g, b);
        push(x3, y3, u3, v3, r, g, b);
    }

    private void push(float x, float y, float u, float v, float r, float g, float b) {
        vertices.put(x).put(y);
        vertices.put(u).put(v);
        vertices.put(r).put(g).put(b);
    }

    private void flushVertices() {
        final int count = vertices.position() / VERTEX_ELEMENTS;

        if (count == 0) {
            return;
        }

        try (var _ = vao.bind()) {
            vbo.update(vertices.flip(), 0);
            glDrawArrays(GL_TRIANGLES, 0, count);
        }

        vertices.clear();
    }

    private static int createTexture(BufferedImage image) {
        var raster = image.getRaster();
        var width = raster.getWidth();
        var height = raster.getHeight();

        var buffer = BufferUtils
            .createByteBuffer(width * height * 3)
            .put((byte[]) raster.getDataElements(0, 0, width, height, null))
            .flip();

        var texture = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, buffer);
        glBindTexture(GL_TEXTURE_2D, 0);

        return texture;
    }

    private BufferedImage loadFontImage() throws IOException {
        try (InputStream is = TextRenderPass.class.getResourceAsStream("font.png")) {
            if (is == null) {
                throw new IOException("Font image not found");
            }
            return ImageIO.read(is);
        }
    }

    private Font loadFontMetadata() throws IOException {
        try (InputStream is = TextRenderPass.class.getResourceAsStream("font.json")) {
            if (is == null) {
                throw new IOException("Font metadata not found");
            }
            return new Gson().fromJson(new InputStreamReader(is), Font.class);
        }
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
