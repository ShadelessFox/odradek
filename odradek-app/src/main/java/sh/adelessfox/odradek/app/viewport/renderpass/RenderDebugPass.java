package sh.adelessfox.odradek.app.viewport.renderpass;

import org.lwjgl.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.gl.*;
import sh.adelessfox.odradek.app.viewport.Camera;
import sh.adelessfox.odradek.app.viewport.Viewport;
import sh.adelessfox.odradek.geometry.ComponentType;
import sh.adelessfox.odradek.geometry.ElementType;
import sh.adelessfox.odradek.math.Vec3;

import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL32.GL_PROGRAM_POINT_SIZE;

public class RenderDebugPass implements RenderPass {
    private static final Logger log = LoggerFactory.getLogger(RenderDebugPass.class);

    private static final int MAX_LINES = 32768;
    private static final int MAX_POINTS = 32768;

    private static final int VERTEX_BUFFER_SIZE = 4096;
    private static final int VERTEX_SIZE = 7;

    // region Shaders
    private static final ShaderSource VERTEX_SHADER = new ShaderSource("main.vert", """
        #version 330 core
        
        in vec3 in_position;
        in vec4 in_color_point_size;
        
        out vec3 io_color;
        
        uniform mat4 u_view;
        uniform mat4 u_projection;
        
        void main() {
            gl_Position = u_projection * u_view * vec4(in_position, 1.0);
            gl_PointSize = in_color_point_size.w;
            io_color = in_color_point_size.rgb;
        }""");

    private static final ShaderSource FRAGMENT_SHADER = new ShaderSource("main.frag", """
        #version 330 core
        
        in vec3 io_color;
        
        out vec4 out_color;
        
        void main() {
            out_color = vec4(io_color, 1.0);
        }""");
    // endregion

    private ShaderProgram program;
    private VertexArray vao;
    private VertexBuffer vbo;

    private final FloatBuffer vertices = BufferUtils.createFloatBuffer(VERTEX_BUFFER_SIZE * VERTEX_SIZE);
    private final Deque<Line> lines = new ArrayDeque<>(MAX_LINES);
    private final List<Point> points = new ArrayList<>(MAX_POINTS);

    @Override
    public void init() {
        program = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);

        try (var _ = vao = new VertexArray().bind()) {
            var attributes = List.of(
                new VertexAttribute(0, ElementType.VEC3, ComponentType.FLOAT, 0, 28, false),
                new VertexAttribute(1, ElementType.VEC4, ComponentType.FLOAT, 12, 28, false)
            );

            try (var _ = vbo = vao.createBuffer(attributes)) {
                vbo.allocate(vertices.capacity() * Float.BYTES, GL_STREAM_DRAW);
            }
        }

        // Required since the shader uses gl_PointSize
        glEnable(GL_PROGRAM_POINT_SIZE);
    }

    @Override
    public void dispose() {
        program.dispose();
        vao.dispose();
    }

    @Override
    public void draw(Viewport viewport, double dt) {
        Camera camera = viewport.getCamera();
        if (camera == null) {
            return;
        }
        try (var _ = program.bind()) {
            program.set("u_view", camera.view());
            program.set("u_projection", camera.projection());
            drawLines();
            drawPoints();
        }
    }

    public void point(Vec3 position, Vec3 color, float size, boolean depthTest) {
        point(position.x(), position.y(), position.z(), color.x(), color.y(), color.z(), size, depthTest);
    }

    public void point(float x, float y, float z, float r, float g, float b, float size, boolean depthTest) {
        if (points.size() >= MAX_POINTS) {
            log.warn("Max points reached, skipping further point draws");
            return;
        }

        points.add(new Point(x, y, z, r, g, b, size, depthTest));
    }

    public void line(Vec3 from, Vec3 to, Vec3 color, boolean depthTest) {
        line(from.x(), from.y(), from.z(), to.x(), to.y(), to.z(), color.x(), color.y(), color.z(), depthTest);
    }

    public void line(float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, boolean depthTest) {
        if (lines.size() >= MAX_LINES) {
            log.warn("Max lines reached, skipping further line draws");
            return;
        }

        lines.offer(new Line(
            x1, y1, z1,
            x2, y2, z2,
            r, g, b,
            depthTest
        ));
    }

    public void box(Vec3[] points, Vec3 color, boolean depthTest) {
        for (int i = 0; i < 4; i++) {
            line(points[i], points[(i + 1) % 4], color, depthTest);
            line(points[i + 4], points[(i + 1) % 4 + 4], color, depthTest);
            line(points[i], points[i + 4], color, depthTest);
        }
    }

    public void aabb(Vec3 min, Vec3 max, Vec3 color, boolean depthTest) {
        final Vec3[] points = {
            new Vec3(min.x(), min.y(), min.z()),
            new Vec3(max.x(), min.y(), min.z()),
            new Vec3(max.x(), max.y(), min.z()),
            new Vec3(min.x(), max.y(), min.z()),
            new Vec3(min.x(), min.y(), max.z()),
            new Vec3(max.x(), min.y(), max.z()),
            new Vec3(max.x(), max.y(), max.z()),
            new Vec3(min.x(), max.y(), max.z()),
        };

        box(points, color, depthTest);
    }

    public void cross(Vec3 center, float length, boolean depthTest) {
        float cx = center.x();
        float cy = center.y();
        float cz = center.z();
        float hl = length * 0.5f;

        line(cx - hl, cy, cz, cx + hl, cy, cz, 1, 0, 0, depthTest);
        line(cx, cy - hl, cz, cx, cy + hl, cz, 0, 1, 0, depthTest);
        line(cx, cy, cz - hl, cx, cy, cz + hl, 0, 0, 1, depthTest);
    }

    private void drawPoints() {
        for (Point point : points) {
            if (!point.depthTest) {
                push(point, false);
            }
        }

        flushVertices(false, GL_POINTS);

        for (Point point : points) {
            if (point.depthTest) {
                push(point, true);
            }
        }

        flushVertices(true, GL_POINTS);

        points.clear();
    }

    private void drawLines() {
        for (Line line : lines) {
            if (!line.depthTest) {
                push(line, false);
            }
        }

        flushVertices(false, GL_LINES);

        for (Line line : lines) {
            if (line.depthTest) {
                push(line, true);
            }
        }

        flushVertices(true, GL_LINES);

        lines.clear();
    }

    private void flushVertices(boolean depthTest, int mode) {
        if (depthTest) {
            glEnable(GL_DEPTH_TEST);
        } else {
            glDisable(GL_DEPTH_TEST);
        }

        int count = vertices.position() / VERTEX_SIZE;

        if (count == 0) {
            return;
        }

        try (var _ = vao.bind()) {
            vbo.update(vertices.flip(), 0);
            glDrawArrays(mode, 0, count);
        }

        vertices.clear();
    }

    private void push(Point value, boolean depthTest) {
        if (vertices.remaining() < VERTEX_SIZE) {
            flushVertices(depthTest, GL_POINTS);
        }

        vertices.put(value.x).put(value.y).put(value.z);
        vertices.put(value.r).put(value.g).put(value.b).put(value.size);
    }

    private void push(Line value, boolean depthTest) {
        if (vertices.remaining() < VERTEX_SIZE * 2) {
            flushVertices(depthTest, GL_LINES);
        }

        vertices.put(value.x1).put(value.y1).put(value.z1);
        vertices.put(value.c1).put(value.c2).put(value.c3).put(1.0f);

        vertices.put(value.x2).put(value.y2).put(value.z2);
        vertices.put(value.c1).put(value.c2).put(value.c3).put(1.0f);
    }

    private record Line(
        float x1, float y1, float z1,
        float x2, float y2, float z2,
        float c1, float c2, float c3,
        boolean depthTest
    ) {
    }

    private record Point(
        float x, float y, float z,
        float r, float g, float b,
        float size,
        boolean depthTest
    ) {
    }
}
