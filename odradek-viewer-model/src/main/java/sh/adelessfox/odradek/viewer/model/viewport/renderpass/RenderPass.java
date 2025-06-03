package sh.adelessfox.odradek.viewer.model.viewport.renderpass;

import sh.adelessfox.odradek.viewer.model.viewport.Viewport;

public interface RenderPass {
    /**
     * This method is called when the OpenGL context is created.
     * <p>
     * It's responsible for creating the OpenGL resources needed for the pass.
     */
    void init();

    /**
     * This method is called when the OpenGL context is destroyed.
     * <p>
     * It's responsible for disposing of the OpenGL resources created in {@link #init()}.
     */
    void dispose();

    /**
     * Draws the render pass.
     *
     * @param viewport the viewport to draw to
     * @param dt       the delta time since the last frame
     */
    void draw(Viewport viewport, double dt);
}
