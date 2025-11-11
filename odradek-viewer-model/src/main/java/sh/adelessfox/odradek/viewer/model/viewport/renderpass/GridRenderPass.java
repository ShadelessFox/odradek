package sh.adelessfox.odradek.viewer.model.viewport.renderpass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.opengl.ShaderProgram;
import sh.adelessfox.odradek.opengl.ShaderSource;
import sh.adelessfox.odradek.opengl.VertexArray;
import sh.adelessfox.odradek.viewer.model.viewport.Camera;
import sh.adelessfox.odradek.viewer.model.viewport.Viewport;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;

public class GridRenderPass implements RenderPass {
    private static final Logger log = LoggerFactory.getLogger(GridRenderPass.class);

    private ShaderProgram program;
    private VertexArray vao;

    @Override
    public void init() {
        try {
            program = new ShaderProgram(
                ShaderSource.fromResource(getClass().getResource("/assets/shaders/grid.vert")),
                ShaderSource.fromResource(getClass().getResource("/assets/shaders/grid.frag"))
            );
        } catch (IOException e) {
            log.error("Failed to load shaders", e);
        }

        vao = VertexArray.createPlane(2.0f, 2.0f);
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

        try (var program = this.program.bind()) {
            program.set("u_view", camera.view());
            program.set("u_projection", camera.projection());

            try (var _ = vao.bind()) {
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                glDepthMask(false);

                glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

                glDisable(GL_BLEND);
                glDepthMask(true);
            }
        }
    }
}
