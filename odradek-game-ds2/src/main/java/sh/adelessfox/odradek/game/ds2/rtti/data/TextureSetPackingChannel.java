package sh.adelessfox.odradek.game.ds2.rtti.data;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.rtti.data.Value;

import java.util.Optional;

public record TextureSetPackingChannel(Optional<DS2.ETextureSetChannel> channel, DS2.ETextureSetType type) {
    public static Optional<TextureSetPackingChannel> of(int packingInfo) {
        var type = DS2.ETextureSetType.valueOf(packingInfo & 0xf);
        if (type == DS2.ETextureSetType.Invalid) {
            return Optional.empty();
        }

        Optional<DS2.ETextureSetChannel> channel;
        if ((packingInfo & 0x80) == 0) {
            channel = Value.valueOf(DS2.ETextureSetChannel.class, packingInfo >>> 4).tryUnwrap();
        } else {
            channel = Optional.empty();
        }

        return Optional.of(new TextureSetPackingChannel(channel, type));
    }

    @Override
    public String toString() {
        return channel.map(ch -> type.name() + '.' + ch).orElseGet(type::name);
    }
}
