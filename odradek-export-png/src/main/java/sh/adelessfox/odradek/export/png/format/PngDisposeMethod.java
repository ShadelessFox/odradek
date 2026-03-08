package sh.adelessfox.odradek.export.png.format;

public enum PngDisposeMethod {
    /**
     * No disposal is done on this frame before rendering the next;
     * the contents of the output buffer are left as is.
     */
    NONE,

    /**
     * The frame's region of the output buffer is to be cleared to
     * fully transparent black before rendering the next frame.
     */
    BACKGROUND,

    /**
     * The frame's region of the output buffer is to be reverted to
     * the previous contents before rendering the next frame.
     */
    PREVIOUS
}
