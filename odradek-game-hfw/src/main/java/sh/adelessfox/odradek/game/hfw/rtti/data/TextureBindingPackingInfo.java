package sh.adelessfox.odradek.game.hfw.rtti.data;

sealed public interface TextureBindingPackingInfo {
    record Texture() implements TextureBindingPackingInfo {
    }

    record TextureSet(PackingInfo packingInfo) implements TextureBindingPackingInfo {
    }

    static TextureBindingPackingInfo of(int data) {
        return switch ((data & 3)) {
            case 1 -> new Texture();
            case 2 -> new TextureSet(PackingInfo.of(data >>> 2));
            default -> throw new IllegalStateException("Unexpected binding type: " + data);
        };
    }
}
