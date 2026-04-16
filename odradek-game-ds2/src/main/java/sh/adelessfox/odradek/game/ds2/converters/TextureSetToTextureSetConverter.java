package sh.adelessfox.odradek.game.ds2.converters;

import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.game.ds2.rtti.data.TextureSetPacking;
import sh.adelessfox.odradek.game.ds2.rtti.data.TextureSetPackingChannel;
import sh.adelessfox.odradek.texture.Channel;
import sh.adelessfox.odradek.texture.Texture;
import sh.adelessfox.odradek.texture.TextureColorSpace;
import sh.adelessfox.odradek.texture.TextureSet;

import java.util.Optional;

public class TextureSetToTextureSetConverter
    implements Converter<DS2.TextureSet, TextureSet, DS2Game> {

    @Override
    public Optional<TextureSet> convert(DS2.TextureSet object, DS2Game game) {
        var sourceTextures = object.textureDesc().stream()
            .map(TextureSetToTextureSetConverter::mapSourceTexture)
            .flatMap(Optional::stream)
            .toList();

        var packedTextures = object.entries().stream()
            .map(entry -> mapPackedTexture(entry, game))
            .flatMap(Optional::stream)
            .toList();

        return Optional.of(new TextureSet(
            sourceTextures,
            packedTextures
        ));
    }

    private static Optional<TextureSet.SourceTexture> mapSourceTexture(DS2.TextureSetTextureDesc desc) {
        var name = desc.path().isEmpty() ? desc.textureType().toString() : desc.path().substring("work:".length());
        var type = mapTextureSetType(desc.textureType().unwrap());
        var colorSpace = desc.gammaSpace() ? TextureColorSpace.SRGB : TextureColorSpace.LINEAR;
        return Optional.of(new TextureSet.SourceTexture(name, type, colorSpace));
    }

    private static Optional<TextureSet.PackedTexture> mapPackedTexture(DS2.TextureSetEntry entry, DS2Game game) {
        var texture = Converter.convert(entry.texture().get(), Texture.class, game).orElse(null);
        if (texture == null) {
            return Optional.empty();
        }

        return Optional.of(new TextureSet.PackedTexture(texture, mapPacking(entry.packingInfo())));
    }

    private static TextureSet.Packing mapPacking(int packingInfo) {
        var packing = TextureSetPacking.of(packingInfo);
        return new TextureSet.Packing(
            packing.red().map(TextureSetToTextureSetConverter::mapPackingChannel),
            packing.green().map(TextureSetToTextureSetConverter::mapPackingChannel),
            packing.blue().map(TextureSetToTextureSetConverter::mapPackingChannel),
            packing.alpha().map(TextureSetToTextureSetConverter::mapPackingChannel)
        );
    }

    private static TextureSet.PackingChannel mapPackingChannel(TextureSetPackingChannel packing) {
        return new TextureSet.PackingChannel(
            mapTextureSetType(packing.type()),
            mapTextureSetChannel(packing.channel().orElseThrow())
        );
    }

    private static Channel mapTextureSetChannel(DS2.ETextureSetChannel channel) {
        return switch (channel) {
            case R -> Channel.R;
            case G -> Channel.G;
            case B -> Channel.B;
            case A -> Channel.A;
        };
    }

    private static String mapTextureSetType(DS2.ETextureSetType type) {
        return type.toString();
    }
}
