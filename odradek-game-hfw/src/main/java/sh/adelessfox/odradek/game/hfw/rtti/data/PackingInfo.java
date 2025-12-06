package sh.adelessfox.odradek.game.hfw.rtti.data;

import java.util.Optional;
import java.util.StringJoiner;

public record PackingInfo(
    Optional<PackingInfoChannel> red,
    Optional<PackingInfoChannel> green,
    Optional<PackingInfoChannel> blue,
    Optional<PackingInfoChannel> alpha
) {
    public static PackingInfo of(int packingInfo) {
        return new PackingInfo(
            PackingInfoChannel.of(packingInfo & 0xff),
            PackingInfoChannel.of(packingInfo >>> 8 & 0xff),
            PackingInfoChannel.of(packingInfo >>> 16 & 0xff),
            PackingInfoChannel.of(packingInfo >>> 24 & 0xff)
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
