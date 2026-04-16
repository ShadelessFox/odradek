package sh.adelessfox.odradek.game.hfw.converters;

import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.HFWGame;
import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.game.hfw.rtti.HFW.ETextureSetChannel;
import sh.adelessfox.odradek.game.hfw.rtti.HFW.ETextureSetType;
import sh.adelessfox.odradek.game.hfw.rtti.HFW.TextureSetEntry;
import sh.adelessfox.odradek.game.hfw.rtti.HFW.TextureSetTextureDesc;
import sh.adelessfox.odradek.game.hfw.rtti.data.TextureSetPacking;
import sh.adelessfox.odradek.game.hfw.rtti.data.TextureSetPackingChannel;
import sh.adelessfox.odradek.texture.Channel;
import sh.adelessfox.odradek.texture.Texture;
import sh.adelessfox.odradek.texture.TextureColorSpace;
import sh.adelessfox.odradek.texture.TextureSet;

import java.util.Optional;

public class TextureSetToTextureSetConverter
    implements Converter<HFW.TextureSet, TextureSet, HFWGame> {

    @Override
    public Optional<TextureSet> convert(HFW.TextureSet object, HFWGame game) {
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

    private static Optional<TextureSet.SourceTexture> mapSourceTexture(TextureSetTextureDesc desc) {
        var name = desc.path().isEmpty() ? desc.textureType().toString() : desc.path().substring("work:".length());
        var type = mapTextureSetType(desc.textureType().unwrap());
        var colorSpace = desc.gammaSpace() ? TextureColorSpace.SRGB : TextureColorSpace.LINEAR;
        return Optional.of(new TextureSet.SourceTexture(name, type, colorSpace));
    }

    private static Optional<TextureSet.PackedTexture> mapPackedTexture(TextureSetEntry entry, HFWGame game) {
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

    private static Channel mapTextureSetChannel(ETextureSetChannel channel) {
        return switch (channel) {
            case R -> Channel.R;
            case G -> Channel.G;
            case B -> Channel.B;
            case A -> Channel.A;
        };
    }

    private static String mapTextureSetType(ETextureSetType type) {
        return type.toString();
    }
}
