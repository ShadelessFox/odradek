package sh.adelessfox.odradek.texture;

public enum TextureFormat {
    // Uncompressed formats
    R8_UNORM(Block.BITS_8),
    R8G8_UNORM(Block.BITS_16),
    R8G8B8_UNORM(Block.BITS_24),
    R8G8B8A8_UNORM(Block.BITS_32),
    B8G8R8_UNORM(Block.BITS_24),
    B8G8R8A8_UNORM(Block.BITS_32),

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

    public boolean isCompressed() {
        return switch (this) {
            case BC1, BC2, BC3, BC4U, BC4S, BC5U, BC5S, BC6U, BC6S, BC7 -> true;
            default -> false;
        };
    }

    public enum Block {
        BITS_8(1, 1, 1),
        BITS_16(1, 1, 2),
        BITS_24(1, 1, 3),
        BITS_32(1, 1, 4),

        BC1(4, 4, 8),
        BC2(4, 4, 16),
        BC3(4, 4, 16),
        BC4(4, 4, 8),
        BC5(4, 4, 16),
        BC6(4, 4, 16),
        BC7(4, 4, 16);

        private final int width;
        private final int height;
        private final int size;

        Block(int width, int height, int size) {
            this.width = width;
            this.height = height;
            this.size = size;
        }

        public int surfaceSize(int width, int height) {
            var blockWidth = (width + this.width - 1) / this.width;
            var blockHeight = (height + this.height - 1) / this.height;
            return blockWidth * blockHeight * size;
        }

        public int width() {
            return width;
        }

        public int height() {
            return height;
        }

        public int size() {
            return size;
        }
    }
}
