package sh.adelessfox.odradek.viewer.model.viewport.renderpass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.geometry.Primitive;
import sh.adelessfox.odradek.geometry.Semantic;
import sh.adelessfox.odradek.math.Matrix4f;
import sh.adelessfox.odradek.math.Vector3f;
import sh.adelessfox.odradek.opengl.*;
import sh.adelessfox.odradek.opengl.rhi.AddressMode;
import sh.adelessfox.odradek.opengl.rhi.FilterMode;
import sh.adelessfox.odradek.opengl.rhi.SamplerDescriptor;
import sh.adelessfox.odradek.scene.Node;
import sh.adelessfox.odradek.scene.Scene;
import sh.adelessfox.odradek.viewer.model.viewport.Camera;
import sh.adelessfox.odradek.viewer.model.viewport.Viewport;

import javax.imageio.ImageIO;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;

public final class RenderMeshesPass implements RenderPass {
    private static final Logger log = LoggerFactory.getLogger(RenderMeshesPass.class);

    private static final int FLAG_HAS_NORMAL = 1;
    private static final int FLAG_HAS_UV = 1 << 1;

    private final Map<Node, GpuNode> cache = new IdentityHashMap<>();

    private ShaderProgram program;
    private Texture diffuseTexture;
    private Sampler diffuseSampler;
    private Scene scene;

    @Override
    public void init() {
        try {
            program = new ShaderProgram(
                ShaderSource.fromResource(getClass().getResource("/assets/shaders/mesh.vert")),
                ShaderSource.fromResource(getClass().getResource("/assets/shaders/mesh.frag"))
            );
        } catch (IOException e) {
            log.error("Failed to load shaders", e);
            return;
        }

        try {
            diffuseTexture = Texture.load(loadImage());
            diffuseSampler = diffuseTexture.createSampler(new SamplerDescriptor(
                AddressMode.REPEAT,
                AddressMode.REPEAT,
                FilterMode.NEAREST,
                FilterMode.NEAREST
            ));
        } catch (IOException e) {
            log.error("Unable to load the UV texture", e);
            if (diffuseTexture != null) {
                diffuseTexture.dispose();
                diffuseTexture = null;
            }
            if (diffuseSampler != null) {
                diffuseSampler.dispose();
                diffuseSampler = null;
            }
        }
    }

    @Override
    public void dispose() {
        cache.clear();
        program.dispose();
        diffuseTexture.dispose();
        diffuseSampler.dispose();
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
        renderScene(activeCamera, scene, viewport.isKeyDown(KeyEvent.VK_X));
    }

    private void renderScene(Camera camera, Scene scene, boolean wireframe) {
        try (var program = this.program.bind();
             var _ = this.diffuseSampler.bind()
        ) {
            program.set("u_view", camera.view());
            program.set("u_projection", camera.projection());
            program.set("u_view_position", camera.position());

            for (Node node : scene.nodes()) {
                renderNode(node, node.matrix(), camera, wireframe);
            }
        }
    }

    private void renderNode(Node node, Matrix4f transform, Camera camera, boolean wireframe) {
        glEnable(GL_DEPTH_TEST);
        glPolygonMode(GL_FRONT_AND_BACK, wireframe ? GL_LINE : GL_FILL);

        if (node.mesh().isPresent()) {
            GpuNode data = cache.computeIfAbsent(node, this::uploadNode);

            for (GpuPrimitive primitive : data.primitives()) {
                int flags = primitive.buildFlags();

                program.set("u_model", transform);
                program.set("u_color", primitive.color);
                program.set("u_texture", diffuseSampler);
                program.set("u_flags", flags);

                try (VertexArray ignored = primitive.vao.bind()) {
                    glDrawElements(GL_TRIANGLES, primitive.count(), primitive.type(), 0);
                }
            }
        }

        for (Node child : node.children()) {
            renderNode(child, transform.mul(child.matrix()), camera, wireframe);
        }

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
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
        var semantics = new HashSet<Semantic>();

        for (Semantic semantic : List.of(Semantic.POSITION, Semantic.NORMAL, Semantic.TEXTURE_0)) {
            var accessor = vertices.get(semantic);
            var location1 = location++;
            if (accessor == null) {
                continue;
            }
            var attributes = buffers.computeIfAbsent(accessor.buffer(), _ -> new ArrayList<>());
            attributes.add(new VertexAttribute(
                location1,
                accessor.elementType(),
                accessor.componentType(),
                accessor.offset(),
                accessor.stride(),
                accessor.normalized()
            ));
            semantics.add(semantic);
        }

        if (!semantics.contains(Semantic.POSITION)) {
            log.error("Missing required vertex attribute: {}", Semantic.POSITION);
            return Optional.empty();
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

            var random = new Random(primitive.hash());
            var color = new Vector3f(
                random.nextFloat(0.5f, 1.0f),
                random.nextFloat(0.5f, 1.0f),
                random.nextFloat(0.5f, 1.0f)
            );

            return Optional.of(new GpuPrimitive(count, type, vao, color, semantics));
        }
    }

    private BufferedImage loadImage() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/assets/textures/color.png")) {
            if (is == null) {
                throw new IOException("Image not found");
            }
            return ImageIO.read(is);
        }
    }

    private record GpuNode(List<GpuPrimitive> primitives) {
        void dispose() {
            for (GpuPrimitive primitive : primitives) {
                primitive.dispose();
            }
        }
    }

    private record GpuPrimitive(int count, int type, VertexArray vao, Vector3f color, Set<Semantic> semantics) {
        private GpuPrimitive {
            semantics = Set.copyOf(semantics);
        }

        int buildFlags() {
            int flags = 0;
            if (semantics.contains(Semantic.NORMAL)) {
                flags |= FLAG_HAS_NORMAL;
            }
            if (semantics.contains(Semantic.TEXTURE_0)) {
                flags |= FLAG_HAS_UV;
            }
            return flags;
        }

        void dispose() {
            vao.dispose();
        }
    }
}
