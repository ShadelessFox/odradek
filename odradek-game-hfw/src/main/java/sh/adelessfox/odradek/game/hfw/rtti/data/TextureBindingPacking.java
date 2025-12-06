package sh.adelessfox.odradek.game.hfw.rtti.data;

sealed public interface TextureBindingPacking {
    record Texture() implements TextureBindingPacking {
    }

    record TextureSet(TextureSetPacking packingInfo) implements TextureBindingPacking {
    }

    static TextureBindingPacking of(int data) {
        return switch ((data & 3)) {
            case 1 -> new Texture();
            case 2 -> new TextureSet(TextureSetPacking.of(data >>> 2));
            default -> throw new IllegalStateException("Unexpected binding type: " + data);
        };
    }
}
