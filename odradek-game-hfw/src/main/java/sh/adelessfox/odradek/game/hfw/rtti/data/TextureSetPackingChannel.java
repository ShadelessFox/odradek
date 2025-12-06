package sh.adelessfox.odradek.game.hfw.rtti.data;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.ETextureSetChannel;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.ETextureSetType;

import java.util.Optional;

public record TextureSetPackingChannel(Optional<ETextureSetChannel> channel, ETextureSetType type) {
    public static Optional<TextureSetPackingChannel> of(int packingInfo) {
        var type = ETextureSetType.valueOf(packingInfo & 0xf);
        if (type == ETextureSetType.Invalid) {
            return Optional.empty();
        }

        Optional<ETextureSetChannel> channel;
        if ((packingInfo & 0x80) == 0) {
            channel = Optional.of(ETextureSetChannel.valueOf(packingInfo >>> 4));
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
