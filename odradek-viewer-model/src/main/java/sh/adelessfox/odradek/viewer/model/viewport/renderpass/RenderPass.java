package sh.adelessfox.odradek.viewer.model.viewport.renderpass;

import sh.adelessfox.odradek.viewer.model.viewport.Viewport;
import sh.adelessfox.odradek.viewer.model.viewport.ViewportInput;

public interface RenderPass {
    /**
     * This method is called when the OpenGL context is created.
     * <p>
     * It's responsible for creating the OpenGL resources needed for the pass.
     * <p>
     * This method may throw, but really shouldn't, exceptions on failure.
     * If this happens, {@link #dispose()} will be called, and this render pass
     * will be excluded from the render queue.
     */
    void init() throws Exception;

    /**
     * This method is called when the OpenGL context is destroyed. It should correctly
     * handle partially initialized render pass in case {@link #init()} throws an exception.
     * <p>
     * It's responsible for disposing of the OpenGL resources created in {@link #init()}.
     */
    void dispose();

    /**
     * Processes the input. Called before drawing.
     *
     * @param viewport the viewport to process input for
     * @param dt       the delta time since the last frame
     * @param input    the input state
     */
    default void process(Viewport viewport, double dt, ViewportInput input) {
        // do nothing by default
    }

    /**
     * Draws the render pass.
     *
     * @param viewport the viewport to draw to
     * @param dt       the delta time since the last frame
     */
    void draw(Viewport viewport, double dt);
}
