package sh.adelessfox.odradek.game.hfw.converters.texture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.EPixelFormat;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.ETexColorSpace;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.ETextureType;
import sh.adelessfox.odradek.texture.Texture;
import sh.adelessfox.odradek.texture.TextureColorSpace;
import sh.adelessfox.odradek.texture.TextureFormat;
import sh.adelessfox.odradek.texture.TextureType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

abstract class BaseTextureConverter<T> implements Converter<T, Texture, ForbiddenWestGame> {
    private static final Logger log = LoggerFactory.getLogger(BaseTextureConverter.class);

    protected static Optional<TextureFormat> mapFormat(EPixelFormat format) {
        return Optional.ofNullable(switch (format) {
            case R_UNORM_8 -> TextureFormat.R8_UNORM;
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
        });
    }

    protected static Optional<TextureType> mapType(ETextureType type) {
        return Optional.ofNullable(switch (type) {
            case _2D -> TextureType.SURFACE;
            case _2DArray -> TextureType.ARRAY;
            case _3D -> TextureType.VOLUME;
            case CubeMap -> TextureType.CUBEMAP;
            default -> {
                log.debug("Unsupported texture type: {}", type);
                yield null;
            }
        });
    }

    protected static TextureColorSpace mapColorSpace(ETexColorSpace colorSpace) {
        return switch (colorSpace) {
            case Linear -> TextureColorSpace.LINEAR;
            case sRGB -> TextureColorSpace.SRGB;
        };
    }

    protected static byte[] readDataSource(ForbiddenWestGame game, HorizonForbiddenWest.StreamingDataSource dataSource) {
        try {
            return game.getStreamingSystem().getDataSourceData(dataSource);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
