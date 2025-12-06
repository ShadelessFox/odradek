package sh.adelessfox.odradek.game.hfw.rtti.data;

import java.util.Optional;
import java.util.StringJoiner;

public record TextureSetPacking(
    Optional<TextureSetPackingChannel> red,
    Optional<TextureSetPackingChannel> green,
    Optional<TextureSetPackingChannel> blue,
    Optional<TextureSetPackingChannel> alpha
) {
    public static TextureSetPacking of(int packingInfo) {
        return new TextureSetPacking(
            TextureSetPackingChannel.of(packingInfo & 0xff),
            TextureSetPackingChannel.of(packingInfo >>> 8 & 0xff),
            TextureSetPackingChannel.of(packingInfo >>> 16 & 0xff),
            TextureSetPackingChannel.of(packingInfo >>> 24 & 0xff)
        );
    }

    @Override
    public String toString() {
        var joiner = new StringJoiner(", ", "PackingInfo[", "]");
        red.ifPresent(r -> joiner.add("R=" + r));
        green.ifPresent(g -> joiner.add("G=" + g));
        blue.ifPresent(b -> joiner.add("B=" + b));
        alpha.ifPresent(a -> joiner.add("A=" + a));
        return joiner.toString();
    }
}
