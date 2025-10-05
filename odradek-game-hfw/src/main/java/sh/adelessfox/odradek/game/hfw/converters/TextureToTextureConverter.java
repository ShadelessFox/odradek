package sh.adelessfox.odradek.game.hfw.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.EPixelFormat;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.ETexColorSpace;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.ETextureType;
import sh.adelessfox.odradek.texture.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

public class TextureToTextureConverter implements Converter<ForbiddenWestGame, Texture> {
    private static final Logger log = LoggerFactory.getLogger(TextureToTextureConverter.class);

    @Override
    public Optional<Texture> convert(Object object, ForbiddenWestGame game) {
        return switch (object) {
            case HorizonForbiddenWest.Texture t -> convertTexture(t, game);
            case HorizonForbiddenWest.UITexture t -> convertUiTexture(t, game);
            default -> Optional.empty();
        };
    }

    @Override
    public Set<Class<?>> convertibleTypes() {
        return Set.of(
            HorizonForbiddenWest.Texture.class,
            HorizonForbiddenWest.UITexture.class
        );
    }

    private static Optional<Texture> convertTexture(HorizonForbiddenWest.Texture texture, ForbiddenWestGame game) {
        var format = mapFormat(texture.header().pixelFormat().unwrap());
        var type = mapType(texture.header().type().unwrap());

        if (format == null || type == null) {
            return Optional.empty();
        }

        var textureSet = texture.textureSetParent() != null ? texture.textureSetParent().get() : null;
        var dataSource = textureSet != null ? textureSet.streamingDataSource() : texture.streamingDataSource();
        var streamedData = ByteBuffer.wrap(readDataSource(game, dataSource));
        var embeddedData = ByteBuffer.wrap(texture.data().embeddedData());

        int width = texture.header().width() & 0x3FFF;
        int height = texture.header().height() & 0x3FFF;
        int numMipmaps = Byte.toUnsignedInt(texture.header().numMips());
        int numSurfaces = Short.toUnsignedInt(texture.header().numSurfaces());

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

                if (mip >= texture.data().streamedMips()) {
                    embeddedData.get(surface.data());
                } else if (textureSet == null) {
                    streamedData.get(surface.data());
                } else {
                    streamedData.get(texture.streamingMipOffsets()[mip], surface.data());
                }

                surfaces.add(surface);
            }
        }

        assert !streamedData.hasRemaining() || textureSet != null;
        assert !embeddedData.hasRemaining();

        return Optional.of(new Texture(
            format,
            type,
            mapColorSpace(texture.header().colorSpace().unwrap()),
            surfaces,
            numMipmaps,
            type == TextureType.VOLUME ? OptionalInt.of(1 << numSurfaces) : OptionalInt.empty(),
            type == TextureType.ARRAY ? OptionalInt.of(numSurfaces) : OptionalInt.empty()
        ));
    }

    private static Optional<Texture> convertUiTexture(HorizonForbiddenWest.UITexture texture, ForbiddenWestGame game) {
        if (texture.animated()) {
            log.debug("UITexture {} is animated, skipping conversion", texture);
            return Optional.empty();
        }

        var data = texture.largeTexture();
        var format = mapFormat(data.header().pixelFormat().unwrap());

        if (format == null) {
            return Optional.empty();
        }

        assert data.header().type() == ETextureType._2D;
        assert data.header().numMips() == 1;
        assert data.header().numSurfaces() == 0;
        assert data.data().streamedMips() == 0;

        int width = data.header().width() & 0x3FFF;
        int height = data.header().height() & 0x3FFF;
        var surface = Surface.create(width, height, format, data.data().embeddedData());

        return Optional.of(Texture.of2D(
            format,
            mapColorSpace(data.header().colorSpace().unwrap()),
            surface
        ));
    }

    private static TextureFormat mapFormat(EPixelFormat format) {
        return switch (format) {
            case RGBA_8888 -> TextureFormat.R8G8B8A8_UNORM;
            case BC1 -> TextureFormat.BC1_UNORM;
            case BC2 -> TextureFormat.BC2_UNORM;
            case BC3 -> TextureFormat.BC3_UNORM;
            case BC4U -> TextureFormat.BC4_UNORM;
            case BC4S -> TextureFormat.BC4_SNORM;
            case BC5U -> TextureFormat.BC5_UNORM;
            case BC5S -> TextureFormat.BC5_SNORM;
            case BC6U -> TextureFormat.BC6_UNORM;
            case BC6S -> TextureFormat.BC6_SNORM;
            case BC7 -> TextureFormat.BC7_UNORM;
            default -> {
                log.debug("Unsupported pixel format: {}", format);
                yield null;
            }
        };
    }

    private static TextureColorSpace mapColorSpace(ETexColorSpace colorSpace) {
        return switch (colorSpace) {
            case Linear -> TextureColorSpace.LINEAR;
            case sRGB -> TextureColorSpace.SRGB;
        };
    }

    private static TextureType mapType(ETextureType type) {
        return switch (type) {
            case _2D -> TextureType.SURFACE;
            case _2DArray -> TextureType.ARRAY;
            case _3D -> TextureType.VOLUME;
            case CubeMap -> TextureType.CUBEMAP;
            default -> {
                log.debug("Unsupported texture type: {}", type);
                yield null;
            }
        };
    }

    private static byte[] readDataSource(ForbiddenWestGame game, HorizonForbiddenWest.StreamingDataSource dataSource) {
        try {
            return game.getStreamingSystem().getDataSourceData(dataSource);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
