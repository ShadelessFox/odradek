package sh.adelessfox.odradek.texture;

public enum Channel {
    /** The red channel. */
    R(0),
    /** The green channel. */
    G(1),
    /** The blue channel. */
    B(2),
    /** The alpha channel. */
    A(3);

    private final int index;

    Channel(int index) {
        this.index = index;
    }

    public int index() {
        return index;
    }
}
