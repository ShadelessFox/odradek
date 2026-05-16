package sh.adelessfox.odradek.game.hfw.converters.texture;

import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.HFWGame;
import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.texture.Surface;
import sh.adelessfox.odradek.texture.Texture;
import sh.adelessfox.odradek.texture.TextureKind;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Optional;
import java.util.OptionalInt;

public final class TextureToTextureConverter
    extends BaseTextureConverter<HFW.Texture>
    implements Converter<HFW.Texture, Texture, HFWGame> {

    @Override
    public Optional<Texture> convert(HFW.Texture object, HFWGame game) {
        var format = mapFormat(object.header().pixelFormat().unwrap()).orElse(null);
        var type = mapType(object.header().type().unwrap()).orElse(null);
        if (format == null || type == null) {
            return Optional.empty();
        }

        var textureSet = object.textureSetParent() != null ? object.textureSetParent().get() : null;
        var dataSource = textureSet != null ? textureSet.streamingDataSource() : object.streamingDataSource();
        var streamedData = ByteBuffer.wrap(game.readDataSource(dataSource));
        var embeddedData = ByteBuffer.wrap(object.data().embeddedData());

        int width = object.header().width() & 0x3FFF;
        int height = object.header().height() & 0x3FFF;
        int numMipmaps = Byte.toUnsignedInt(object.header().numMips());
        int numSurfaces = Short.toUnsignedInt(object.header().numSurfaces());
        var colorSpace = mapColorSpace(object.header().colorSpace().unwrap());

        var surfaces = new ArrayList<Surface>();
        for (int mip = 0; mip < numMipmaps; mip++) {
            int mipWidth = Math.max(width >> mip, format.block().width());
            int mipHeight = Math.max(height >> mip, format.block().height());

            int elements = switch (type) {
                case TEXTURE_2D_ARRAY -> numSurfaces;
                case TEXTURE_3D -> 1 << Math.max(0, numSurfaces - mip);
                case CUBE_MAP -> 6;
                default -> 1;
            };

            for (int element = 0; element < elements; element++) {
                var surface = Surface.create(mipWidth, mipHeight, format, colorSpace);

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
            colorSpace,
            surfaces,
            numMipmaps,
            type == TextureKind.TEXTURE_3D ? OptionalInt.of(1 << numSurfaces) : OptionalInt.empty(),
            type == TextureKind.TEXTURE_2D_ARRAY ? OptionalInt.of(numSurfaces) : OptionalInt.empty(),
            Optional.empty()
        ));
    }
}
