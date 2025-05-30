package sh.adelessfox.odradek.texture;

public enum TextureFormat {
    // Compressed formats
    BC1(Block.BC1),
    BC2(Block.BC2),
    BC3(Block.BC3),
    BC4U(Block.BC4),
    BC4S(Block.BC4),
    BC5U(Block.BC5),
    BC5S(Block.BC5),
    BC6U(Block.BC6),
    BC6S(Block.BC6),
    BC7(Block.BC7);

    private final Block block;

    TextureFormat(Block block) {
        this.block = block;
    }

    public Block block() {
        return block;
    }

    public enum Block {
        BC1(4, 4, 4),
        BC2(4, 4, 8),
        BC3(4, 4, 8),
        BC4(4, 4, 4),
        BC5(4, 4, 8),
        BC6(4, 4, 8),
        BC7(4, 4, 8);

        private final int width;
        private final int height;
        private final int bitsPerPixel;

        Block(int width, int height, int bitsPerPixel) {
            this.width = width;
            this.height = height;
            this.bitsPerPixel = bitsPerPixel;
        }

        public int width() {
            return width;
        }

        public int height() {
            return height;
        }

        public int bitsPerPixel() {
            return bitsPerPixel;
        }
    }
}
