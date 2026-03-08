package sh.adelessfox.odradek.export.png.format;

public enum PngBlendMethod {
    /**
     * All color components of the frame, including alpha, overwrite
     * the current contents of the frame's output buffer region.
     */
    SOURCE,

    /**
     * The frame should be composited onto the output buffer based on
     * its alpha, using a simple {@code OVER} operation as described in
     * <a href="https://www.w3.org/TR/png/#13Alpha-channel-processing">Alpha Channel Processing</a>.
     */
    OVER
}
