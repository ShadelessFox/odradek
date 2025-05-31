package sh.adelessfox.odradek.game.hfw.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest;
import sh.adelessfox.odradek.texture.Surface;
import sh.adelessfox.odradek.texture.Texture;
import sh.adelessfox.odradek.texture.TextureFormat;
import sh.adelessfox.odradek.texture.TextureType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Optional;
import java.util.OptionalInt;

public class TextureConverter implements Converter<ForbiddenWestGame, Texture> {
    private static final Logger log = LoggerFactory.getLogger(TextureConverter.class);

    @Override
    public boolean supports(Object object) {
        return object instanceof HorizonForbiddenWest.Texture;
    }

    @Override
    public Optional<Texture> convert(Object object, ForbiddenWestGame game) {
        try {
            return convert((HorizonForbiddenWest.Texture) object, game);
        } catch (IOException e) {
            throw new UncheckedIOException(e); // TODO: Exception handling in Converter
        }
    }

    private static Optional<Texture> convert(HorizonForbiddenWest.Texture texture, ForbiddenWestGame game) throws IOException {
        var format = mapFormat(texture.header().pixelFormat()).orElse(null);
        if (format == null) {
            log.debug("Unsupported texture format: {}", texture.header().pixelFormat());
            return Optional.empty();
        }

        var type = mapType(texture.header().type()).orElse(null);
        if (type == null) {
            log.debug("Unsupported texture type: {}", texture.header().type());
            return Optional.empty();
        }

        var textureSet = texture.textureSetParent() != null ? texture.textureSetParent().get() : null;
        var dataSource = textureSet != null ? textureSet.streamingDataSource() : texture.streamingDataSource();
        var streamedData = ByteBuffer.wrap(game.getStreamingSystem().getDataSourceData(dataSource));
        var embeddedData = ByteBuffer.wrap(texture.data().embeddedData());

        int width = texture.header().width() & 0x3FFF;
        int height = texture.header().height() & 0x3FFF;
        int numMipmaps = Byte.toUnsignedInt(texture.header().numMips());
        int numSurfaces = Short.toUnsignedInt(texture.header().numSurfaces());

        var surfaces = new ArrayList<Surface>();
        for (int mip = 0; mip < numMipmaps; mip++) {
            int mipWidth = Math.max(width >> mip, format.block().width());
            int mipHeight = Math.max(height >> mip, format.block().height());
            int mipSize = Math.toIntExact((long) mipWidth * mipHeight * format.block().bitsPerPixel() / 8);

            int elements = switch (type) {
                case ARRAY -> numSurfaces;
                case VOLUME -> 1 << Math.max(0, numSurfaces - mip);
                case CUBEMAP -> 6;
                default -> 1;
            };

            for (int element = 0; element < elements; element++) {
                var mipData = new byte[mipSize];

                if (mip >= texture.data().streamedMips()) {
                    embeddedData.get(mipData, 0, mipSize);
                } else if (textureSet == null) {
                    streamedData.get(mipData, 0, mipSize);
                } else {
                    streamedData.get(texture.streamingMipOffsets()[mip], mipData, 0, mipSize);
                }

                surfaces.add(new Surface(mipWidth, mipHeight, mipData));
            }
        }

        assert !streamedData.hasRemaining() || textureSet != null;
        assert !embeddedData.hasRemaining();

        return Optional.of(new Texture(
            format,
            type,
            surfaces,
            numMipmaps,
            type == TextureType.VOLUME ? OptionalInt.of(numSurfaces) : OptionalInt.empty(),
            type == TextureType.ARRAY ? OptionalInt.of(numSurfaces) : OptionalInt.empty()
        ));
    }

    private static Optional<TextureFormat> mapFormat(HorizonForbiddenWest.EPixelFormat format) {
        return switch (format) {
            case BC1 -> Optional.of(TextureFormat.BC1);
            case BC2 -> Optional.of(TextureFormat.BC2);
            case BC3 -> Optional.of(TextureFormat.BC3);
            case BC4U -> Optional.of(TextureFormat.BC4U);
            case BC4S -> Optional.of(TextureFormat.BC4S);
            case BC5U -> Optional.of(TextureFormat.BC5U);
            case BC5S -> Optional.of(TextureFormat.BC5S);
            case BC6U -> Optional.of(TextureFormat.BC6U);
            case BC6S -> Optional.of(TextureFormat.BC6S);
            case BC7 -> Optional.of(TextureFormat.BC7);
            default -> Optional.empty();
        };
    }

    private static Optional<TextureType> mapType(HorizonForbiddenWest.ETextureType type) {
        return switch (type) {
            case _2D -> Optional.of(TextureType.SURFACE);
            case _2DArray -> Optional.of(TextureType.ARRAY);
            case _3D -> Optional.of(TextureType.VOLUME);
            case CubeMap -> Optional.of(TextureType.CUBEMAP);
            default -> Optional.empty();
        };
    }
}
