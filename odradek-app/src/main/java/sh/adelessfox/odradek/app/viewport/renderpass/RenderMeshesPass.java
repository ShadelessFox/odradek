package sh.adelessfox.odradek.app.viewport.renderpass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.gl.ShaderProgram;
import sh.adelessfox.odradek.app.gl.ShaderSource;
import sh.adelessfox.odradek.app.gl.VertexArray;
import sh.adelessfox.odradek.app.gl.VertexAttribute;
import sh.adelessfox.odradek.app.viewport.Camera;
import sh.adelessfox.odradek.app.viewport.Viewport;
import sh.adelessfox.odradek.geometry.Primitive;
import sh.adelessfox.odradek.geometry.Semantic;
import sh.adelessfox.odradek.math.Mat4;
import sh.adelessfox.odradek.math.Vec3;
import sh.adelessfox.odradek.scene.Node;
import sh.adelessfox.odradek.scene.Scene;

import java.nio.ByteBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public final class RenderMeshesPass implements RenderPass {
    private static final Logger log = LoggerFactory.getLogger(RenderMeshesPass.class);

    // region Shaders
    private static final ShaderSource VERTEX_SHADER = new ShaderSource("main.vert", """
        #version 330 core
        
        layout (location = 0) in vec3 in_position;
        layout (location = 1) in vec3 in_normal;
        
        out vec3 io_position;
        out vec3 io_normal;
        
        uniform mat4 u_model;
        uniform mat4 u_view;
        uniform mat4 u_projection;
        
        void main() {
            io_position = vec3(u_model * vec4(in_position, 1.0));
            io_normal = mat3(transpose(inverse(u_model))) * in_normal;
        
            gl_Position = u_projection * u_view * u_model * vec4(in_position, 1.0);
        }""");

    private static final ShaderSource FRAGMENT_SHADER = new ShaderSource("main.frag", """
        #version 330 core
        
        in vec3 io_position;
        in vec3 io_normal;
        
        out vec4 out_color;
        
        uniform vec3 u_view_position;
        
        void main() {
            vec3 normal = normalize(io_normal);
            vec3 view = normalize(u_view_position - io_position);
            vec3 color = vec3(abs(dot(view, normal)));
        
            out_color = vec4(color * 0.5 + vec3(0.5), 1.0);
        }""");
    // endregion

    private final Map<Node, GpuNode> cache = new IdentityHashMap<>();

    private ShaderProgram program;
    private Scene scene;

    private DebugRenderPass debug;

    @Override
    public void init() {
        program = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        debug = new DebugRenderPass();
        debug.init();
    }

    @Override
    public void dispose() {
        program.dispose();
        debug.dispose();
    }

    @Override
    public void draw(Viewport viewport, double dt) {
        Camera activeCamera = viewport.getCamera();
        if (activeCamera == null) {
            return;
        }
        Scene activeScene = viewport.getScene();
        if (activeScene != scene) {
            cache.values().forEach(GpuNode::dispose);
            scene = activeScene;
        }
        if (scene == null) {
            return;
        }
        renderScene(activeCamera, scene);
        debug.draw(viewport, dt);
    }

    private void renderScene(Camera camera, Scene scene) {
        try (var program = this.program.bind()) {
            program.set("u_view", camera.view());
            program.set("u_projection", camera.projection());
            program.set("u_view_position", camera.position());

            for (Node node : scene.nodes()) {
                renderNode(node, node.matrix(), camera);
            }
        }
    }

    private void renderNode(Node node, Mat4 transform, Camera camera) {
        GpuNode data = cache.computeIfAbsent(node, this::uploadNode);

        for (GpuPrimitive primitive : data.primitives()) {
            program.set("u_model", transform);

            try (VertexArray ignored = primitive.vao.bind()) {
                glDrawElements(GL_TRIANGLES, primitive.count(), primitive.type(), 0);
            }
        }

        for (Node child : node.children()) {
            renderNode(child, transform.mul(child.matrix()), camera);
        }

        if (node.skin().isPresent()) {
            renderSkin(node.skin().get(), null, camera);
        }
    }

    private void renderSkin(Node node, Node parent, Camera camera) {
        if (parent != null) {
            var translation = node.matrix().translation();
            debug.point(translation, new Vec3(1, 0, 1), 10f, false);
            debug.line(parent.matrix().translation(), translation, new Vec3(0, 1, 0), false);

            if (node.name().isPresent()) {
                float distance = translation.distance(camera.position());
                float size = Math.clamp(16.0f / distance, 4.0f, 16.0f);

                debug.projectedText(node.name().get(), translation, camera.viewProjection(), Vec3.one(), size);
            }
        }

        for (Node child : node.children()) {
            renderSkin(child, node, camera);
        }
    }

    private GpuNode uploadNode(Node node) {
        var primitives = node.mesh().stream()
            .flatMap(mesh -> mesh.primitives().stream())
            .map(this::uploadPrimitive)
            .flatMap(Optional::stream)
            .toList();

        return new GpuNode(primitives);
    }

    private Optional<GpuPrimitive> uploadPrimitive(Primitive primitive) {
        var buffers = new IdentityHashMap<ByteBuffer, List<VertexAttribute>>();

        var vertices = primitive.vertices();
        var indices = primitive.indices();

        int location = 0;

        for (Semantic semantic : List.of(Semantic.POSITION, Semantic.NORMAL)) {
            var accessor = vertices.get(semantic);
            if (accessor == null) {
                log.error("Missing required vertex attribute: {}", semantic);
                return Optional.empty();
            }
            var attributes = buffers.computeIfAbsent(accessor.buffer(), _ -> new ArrayList<>());
            attributes.add(new VertexAttribute(
                location++,
                accessor.elementType(),
                accessor.componentType(),
                accessor.offset(),
                accessor.stride(),
                accessor.normalized()
            ));
        }

        try (var vao = new VertexArray().bind()) {
            buffers.forEach((buffer, attributes) -> {
                var vbo = vao.createBuffer(attributes);
                vbo.put(buffer, GL_STATIC_DRAW);
            });

            var ibo = vao.createIndexBuffer();
            ibo.put(indices.buffer(), GL_STATIC_DRAW);

            var count = indices.count();
            var type = switch (indices.componentType()) {
                case UNSIGNED_BYTE -> GL_UNSIGNED_BYTE;
                case UNSIGNED_SHORT -> GL_UNSIGNED_SHORT;
                case UNSIGNED_INT -> GL_UNSIGNED_INT;
                default -> throw new IllegalArgumentException("unsupported index type");
            };

            return Optional.of(new GpuPrimitive(count, type, vao));
        }
    }

    private record GpuNode(List<GpuPrimitive> primitives) {
        void dispose() {
            for (GpuPrimitive primitive : primitives) {
                primitive.dispose();
            }
        }
    }

    private record GpuPrimitive(int count, int type, VertexArray vao) {
        public void dispose() {
            vao.dispose();
        }
    }
}
