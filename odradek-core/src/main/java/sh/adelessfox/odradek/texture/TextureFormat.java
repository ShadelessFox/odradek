package sh.adelessfox.odradek.texture;

public enum TextureFormat {
    // Uncompressed formats
    R8_UNORM(Block.BITS_8),
    R8G8_UNORM(Block.BITS_16),
    R8G8B8_UNORM(Block.BITS_24),
    R8G8B8A8_UNORM(Block.BITS_32),

    R16_UNORM(Block.BITS_16),
    R16G16_UNORM(Block.BITS_32),
    R16G16B16_UNORM(Block.BITS_48),
    R16G16B16A16_UNORM(Block.BITS_64),

    R16_SFLOAT(Block.BITS_16),
    R16G16_SFLOAT(Block.BITS_32),
    R16G16B16_SFLOAT(Block.BITS_48),
    R16G16B16A16_SFLOAT(Block.BITS_64),

    R32_SFLOAT(Block.BITS_32),
    R32G32_SFLOAT(Block.BITS_64),
    R32G32B32_SFLOAT(Block.BITS_96),
    R32G32B32A32_SFLOAT(Block.BITS_128),

    B8G8R8_UNORM(Block.BITS_24),
    B8G8R8A8_UNORM(Block.BITS_32),

    // Compressed formats
    BC1_UNORM(Block.BC1),
    BC2_UNORM(Block.BC2),
    BC3_UNORM(Block.BC3),
    BC4_UNORM(Block.BC4),
    BC4_SNORM(Block.BC4),
    BC5_UNORM(Block.BC5),
    BC5_SNORM(Block.BC5),
    BC6_UNORM(Block.BC6),
    BC6_SNORM(Block.BC6),
    BC7_UNORM(Block.BC7);

    private final Block block;

    TextureFormat(Block block) {
        this.block = block;
    }

    public Block block() {
        return block;
    }

    public boolean isCompressed() {
        return switch (this) {
            case BC1_UNORM,
                 BC2_UNORM,
                 BC3_UNORM,
                 BC4_UNORM, BC4_SNORM,
                 BC5_UNORM, BC5_SNORM,
                 BC6_UNORM, BC6_SNORM,
                 BC7_UNORM -> true;
            default -> false;
        };
    }

    /**
     * Maps a compressed format to a corresponding uncompressed format.
     *
     * @return the corresponding uncompressed format
     * @throws UnsupportedOperationException if the format is not a {@link #isCompressed() compressed format}.
     */
    public TextureFormat decompressed() {
        return switch (this) {
            case BC1_UNORM, BC2_UNORM, BC3_UNORM, BC7_UNORM -> R8G8B8A8_UNORM;
            case BC4_UNORM, BC4_SNORM -> R8_UNORM;
            case BC5_UNORM, BC5_SNORM -> R8G8_UNORM;
            case BC6_UNORM, BC6_SNORM -> R16G16B16_SFLOAT;
            default -> throw new UnsupportedOperationException(name());
        };
    }

    public int channels() {
        return switch (this) {
            // Uncompressed formats
            case R8_UNORM, R16_UNORM, R16_SFLOAT, R32_SFLOAT -> 1;
            case R8G8_UNORM, R16G16_UNORM, R16G16_SFLOAT, R32G32_SFLOAT -> 2;
            case R8G8B8_UNORM, B8G8R8_UNORM, R16G16B16_UNORM, R16G16B16_SFLOAT, R32G32B32_SFLOAT -> 3;
            case R8G8B8A8_UNORM, B8G8R8A8_UNORM, R16G16B16A16_UNORM, R16G16B16A16_SFLOAT, R32G32B32A32_SFLOAT -> 4;

            // Compressed formats
            case BC1_UNORM, BC2_UNORM, BC3_UNORM, BC7_UNORM -> 4;
            case BC4_UNORM, BC4_SNORM -> 1;
            case BC5_UNORM, BC5_SNORM -> 2;
            case BC6_UNORM, BC6_SNORM -> 3;
        };
    }

    public int bitsPerPixel() {
        return block.size() * 8 / block.width() / block.height();
    }

    public enum Block {
        BITS_8(1, 1, 1),
        BITS_16(1, 1, 2),
        BITS_24(1, 1, 3),
        BITS_32(1, 1, 4),
        BITS_48(1, 1, 6),
        BITS_64(1, 1, 8),
        BITS_96(1, 1, 12),
        BITS_128(1, 1, 16),

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
