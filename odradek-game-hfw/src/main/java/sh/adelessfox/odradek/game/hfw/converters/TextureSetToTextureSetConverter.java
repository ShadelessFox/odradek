package sh.adelessfox.odradek.game.hfw.converters;

import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.ETextureSetType;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.TextureSetEntry;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.TextureSetTextureDesc;
import sh.adelessfox.odradek.game.hfw.rtti.data.TextureSetPacking;
import sh.adelessfox.odradek.texture.Texture;
import sh.adelessfox.odradek.texture.TextureSet;

import java.util.Optional;

public class TextureSetToTextureSetConverter
    implements Converter<HorizonForbiddenWest.TextureSet, TextureSet, ForbiddenWestGame> {

    @Override
    public Optional<TextureSet> convert(HorizonForbiddenWest.TextureSet object, ForbiddenWestGame game) {
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
        // TODO: Check if all paths use 'work' device
        var name = desc.path().isEmpty() ? desc.textureType().toString() : desc.path().substring("work:".length());
        var type = mapTextureSetType(desc.textureType().unwrap()).orElse(null);
        if (type == null) {
            return Optional.empty();
        }
        return Optional.of(new TextureSet.SourceTexture(name, type));
    }

    private static Optional<TextureSet.PackedTexture> mapPackedTexture(TextureSetEntry entry, ForbiddenWestGame game) {
        var texture = Converter.convert(entry.texture().get(), Texture.class, game).orElse(null);
        if (texture == null) {
            return Optional.empty();
        }

        return Optional.of(new TextureSet.PackedTexture(texture, mapChannelPacking(entry.packingInfo())));
    }

    private static Optional<String> mapTextureSetType(ETextureSetType type) {
        return Optional.of(type.toString());
    }

    private static TextureSet.ChannelPacking mapChannelPacking(int packingInfo) {
        var packing = TextureSetPacking.of(packingInfo);
        return new TextureSet.ChannelPacking(
            packing.red().flatMap(ch -> mapTextureSetType(ch.type())),
            packing.green().flatMap(ch -> mapTextureSetType(ch.type())),
            packing.blue().flatMap(ch -> mapTextureSetType(ch.type())),
            packing.alpha().flatMap(ch -> mapTextureSetType(ch.type()))
        );
    }
}
