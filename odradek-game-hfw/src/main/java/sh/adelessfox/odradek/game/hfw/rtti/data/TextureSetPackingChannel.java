package sh.adelessfox.odradek.game.hfw.rtti.data;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.ETextureSetChannel;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.ETextureSetType;
import sh.adelessfox.odradek.rtti.data.Value;

import java.util.Optional;

public record TextureSetPackingChannel(Optional<ETextureSetChannel> channel, ETextureSetType type) {
    public static Optional<TextureSetPackingChannel> of(int packingInfo) {
        var type = ETextureSetType.valueOf(packingInfo & 0xf);
        if (type == ETextureSetType.Invalid) {
            return Optional.empty();
        }

        Optional<ETextureSetChannel> channel;
        if ((packingInfo & 0x80) == 0) {
            channel = Value.valueOf(ETextureSetChannel.class, packingInfo >>> 4).tryUnwrap();
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
