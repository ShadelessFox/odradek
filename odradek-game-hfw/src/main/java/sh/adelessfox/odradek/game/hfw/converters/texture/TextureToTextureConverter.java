package sh.adelessfox.odradek.game.hfw.converters.texture;

import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest;
import sh.adelessfox.odradek.texture.Surface;
import sh.adelessfox.odradek.texture.Texture;
import sh.adelessfox.odradek.texture.TextureType;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Optional;
import java.util.OptionalInt;

public final class TextureToTextureConverter
    extends BaseTextureConverter<HorizonForbiddenWest.Texture>
    implements Converter<HorizonForbiddenWest.Texture, Texture, ForbiddenWestGame> {

    @Override
    public Optional<Texture> convert(HorizonForbiddenWest.Texture object, ForbiddenWestGame game) {
        var format = mapFormat(object.header().pixelFormat().unwrap()).orElse(null);
        var type = mapType(object.header().type().unwrap()).orElse(null);
        if (format == null || type == null) {
            return Optional.empty();
        }

        var textureSet = object.textureSetParent() != null ? object.textureSetParent().get() : null;
        var dataSource = textureSet != null ? textureSet.streamingDataSource() : object.streamingDataSource();
        var streamedData = ByteBuffer.wrap(readDataSource(game, dataSource));
        var embeddedData = ByteBuffer.wrap(object.data().embeddedData());

        int width = object.header().width() & 0x3FFF;
        int height = object.header().height() & 0x3FFF;
        int numMipmaps = Byte.toUnsignedInt(object.header().numMips());
        int numSurfaces = Short.toUnsignedInt(object.header().numSurfaces());

        var surfaces = new ArrayList<Surface>();
        for (int mip = 0; mip < numMipmaps; mip++) {
            int mipWidth = Math.max(width >> mip, format.block().width());
            int mipHeight = Math.max(height >> mip, format.block().height());

            int elements = switch (type) {
                case ARRAY -> numSurfaces;
                case VOLUME -> 1 << Math.max(0, numSurfaces - mip);
                case CUBEMAP -> 6;
                default -> 1;
            };

            for (int element = 0; element < elements; element++) {
                var surface = Surface.create(mipWidth, mipHeight, format);

                if (mip >= object.data().streamedMips()) {
                    embeddedData.get(surface.data());
                } else if (textureSet == null) {
                    streamedData.get(surface.data());
                } else {
                    streamedData.get(object.streamingMipOffsets()[mip], surface.data());
                }

                surfaces.add(surface);
            }
        }

        assert !streamedData.hasRemaining() || textureSet != null;
        assert !embeddedData.hasRemaining();

        return Optional.of(new Texture(
            format,
            type,
            mapColorSpace(object.header().colorSpace().unwrap()),
            surfaces,
            numMipmaps,
            type == TextureType.VOLUME ? OptionalInt.of(1 << numSurfaces) : OptionalInt.empty(),
            type == TextureType.ARRAY ? OptionalInt.of(numSurfaces) : OptionalInt.empty(),
            Optional.empty()
        ));
    }
}
