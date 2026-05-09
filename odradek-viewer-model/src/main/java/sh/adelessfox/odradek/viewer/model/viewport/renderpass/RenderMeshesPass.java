package sh.adelessfox.odradek.viewer.model.viewport.renderpass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.geometry.Mesh;
import sh.adelessfox.odradek.geometry.Semantic;
import sh.adelessfox.odradek.geometry.Type;
import sh.adelessfox.odradek.math.Frustum;
import sh.adelessfox.odradek.opengl.*;
import sh.adelessfox.odradek.rhi.AddressMode;
import sh.adelessfox.odradek.rhi.FilterMode;
import sh.adelessfox.odradek.rhi.SamplerDescriptor;
import sh.adelessfox.odradek.scene.Node;
import sh.adelessfox.odradek.scene.Scene;
import sh.adelessfox.odradek.viewer.model.viewport.Camera;
import sh.adelessfox.odradek.viewer.model.viewport.Viewport;
import sh.adelessfox.odradek.viewer.model.viewport.ViewportContext;
import wtf.reversed.toolbox.math.Bounds;
import wtf.reversed.toolbox.math.Matrix4;
import wtf.reversed.toolbox.math.Vector3;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.lwjgl.opengl.GL11.*;

public final class RenderMeshesPass implements RenderPass {
    private static final Logger log = LoggerFactory.getLogger(RenderMeshesPass.class);

    private static final int FLAG_HAS_NORMAL = 1;
    private static final int FLAG_HAS_UV = 1 << 1;
    private static final int FLAG_HAS_COLOR = 1 << 2;
    private static final int FLAG_WIREFRAME = 1 << 3;

    private final List<GpuNode> nodes = new ArrayList<>();

    private ShaderProgram program;
    private Texture diffuseTexture;
    private Sampler diffuseSampler;
    private Scene scene;

    @Override
    public void init() throws IOException {
        program = ShaderProgram.ofVertexFragment(
            ShaderSource.read(getClass().getResource("/assets/shaders/mesh.vert")),
            ShaderSource.read(getClass().getResource("/assets/shaders/mesh.frag"))
        );
        diffuseTexture = Texture.load(loadImage());
        diffuseSampler = diffuseTexture.createSampler(new SamplerDescriptor(
            AddressMode.REPEAT,
            AddressMode.REPEAT,
            FilterMode.NEAREST,
            FilterMode.NEAREST
        ));
    }

    @Override
    public void dispose() {
        clearCache();
        if (program != null) {
            program.dispose();
        }
        if (diffuseSampler != null) {
            diffuseSampler.dispose();
        }
        if (diffuseTexture != null) {
            diffuseTexture.dispose();
        }
        scene = null;
    }

    @Override
    public void draw(Viewport viewport, ViewportContext context, double dt) {
        Camera activeCamera = viewport.getCamera();
        if (activeCamera == null) {
            return;
        }

        Scene activeScene = viewport.getScene();
        if (activeScene != scene) {
            changeScene(activeScene);
        }

        if (scene != null) {
            renderScene(activeCamera, context);
        }
    }

    private void renderScene(Camera camera, ViewportContext context) {
        glEnable(GL_DEPTH_TEST);
        glPolygonMode(GL_FRONT_AND_BACK, context.isShowWireframe() ? GL_LINE : GL_FILL);

        try (var program = this.program.bind()) {
            program.set("u_view", camera.view());
            program.set("u_projection", camera.projection());
            program.set("u_view_position", camera.position());

            var frustum = Frustum.of(camera.projectionView());
            for (GpuNode node : nodes) {
                renderNode(node, frustum, context);
            }
        }

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    }

    private void renderNode(GpuNode node, Frustum frustum, ViewportContext context) {
        // FIXME frustum test is incorrect! Is something wrong with bounding boxes?
        // if (!frustum.test(node.bbox())) {
        //     return;
        // }
        for (GpuPrimitive primitive : node.primitives()) {
            program.set("u_model", node.transform());
            program.set("u_color", primitive.color());
            program.set("u_texture", diffuseSampler);
            program.set("u_flags", buildPrimitiveFlags(primitive, context));

            try (var _ = primitive.vao().bind()) {
                glDrawElements(GL_TRIANGLES, primitive.count(), primitive.type(), 0);
            }
        }
    }

    static int buildPrimitiveFlags(GpuPrimitive primitive, ViewportContext context) {
        int flags = 0;
        if (context.isShowWireframe()) {
            flags |= FLAG_WIREFRAME;
        }
        if (context.isShowVertexUVs() && primitive.semantics.contains(Semantic.TEXTURE_0)) {
            flags |= FLAG_HAS_UV;
        }
        if (context.isShowVertexColors() && primitive.semantics.contains(Semantic.COLOR)) {
            flags |= FLAG_HAS_COLOR;
        }
        if (primitive.semantics.contains(Semantic.NORMAL)) {
            flags |= FLAG_HAS_NORMAL;
        }
        return flags;
    }

    private GpuNode uploadNode(Node node, Matrix4 transform) {
        var primitives = node.mesh().stream()
            .flatMap(mesh -> mesh.meshes().stream())
            .map(this::uploadPrimitive)
            .flatMap(Optional::stream)
            .toList();

        var bbox = node.computeBoundingBox()
            .map(b -> b.transform(transform))
            .orElse(Bounds.EMPTY);

        return new GpuNode(primitives, transform, bbox);
    }

    private Optional<GpuPrimitive> uploadPrimitive(Mesh mesh) {
        try (var vao = new VertexArray().bind()) {
            var vbo = vao.createVertexBuffer(List.of(new VertexAttribute(0, new Type.F32(3), 0, 12)), 0);
            vbo.put(mesh.positions().asBuffer(), 0);

            var ibo = vao.createElementBuffer();
            ibo.put(mesh.indices().asBuffer(), 0);

            return Optional.of(new GpuPrimitive(mesh.indices().length(), GL_UNSIGNED_INT, vao, mesh.color(), Set.of()));
        }
    }

    private void changeScene(Scene scene) {
        clearCache();
        this.scene = scene;
        if (scene != null) {
            cacheScene(scene);
        }
    }

    private void clearCache() {
        nodes.forEach(GpuNode::dispose);
        nodes.clear();
    }

    private void cacheScene(Scene scene) {
        for (Node node : scene.nodes()) {
            cacheNodeRecursively(node, node.matrix());
        }
    }

    private void cacheNodeRecursively(Node node, Matrix4 transform) {
        if (node.mesh().isPresent()) {
            nodes.add(uploadNode(node, transform));
        }
        for (Node child : node.children()) {
            cacheNodeRecursively(child, transform.multiply(child.matrix()));
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

    private record GpuNode(List<GpuPrimitive> primitives, Matrix4 transform, Bounds bbox) {
        void dispose() {
            for (GpuPrimitive primitive : primitives) {
                primitive.dispose();
            }
        }
    }

    private record GpuPrimitive(int count, int type, VertexArray vao, Vector3 color, Set<Semantic> semantics) {
        private GpuPrimitive {
            semantics = Set.copyOf(semantics);
        }

        void dispose() {
            vao.dispose();
        }
    }
}
