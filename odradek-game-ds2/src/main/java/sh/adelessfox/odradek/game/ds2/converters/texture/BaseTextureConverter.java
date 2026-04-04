package sh.adelessfox.odradek.game.ds2.converters.texture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.EPixelFormat;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.ETexColorSpace;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.ETextureType;
import sh.adelessfox.odradek.texture.Texture;
import sh.adelessfox.odradek.texture.TextureColorSpace;
import sh.adelessfox.odradek.texture.TextureFormat;
import sh.adelessfox.odradek.texture.TextureType;

import java.util.Optional;

abstract sealed class BaseTextureConverter<T> implements Converter<T, Texture, DS2Game>
    permits TextureToTextureConverter, UITextureToTextureConverter {

    private static final Logger log = LoggerFactory.getLogger(BaseTextureConverter.class);

    protected static Optional<TextureFormat> mapFormat(EPixelFormat format) {
        return Optional.ofNullable(switch (format) {
            case R_UNORM_8 -> TextureFormat.R8_UNORM;
            case R_UNORM_16 -> TextureFormat.R16_UNORM;
            case RGBA_8888 -> TextureFormat.R8G8B8A8_UNORM;
            case RGBA_8888_REV -> TextureFormat.B8G8R8A8_UNORM;
            case RGBA_FLOAT_16 -> TextureFormat.R16G16B16A16_SFLOAT;
            case R_FLOAT_32 -> TextureFormat.R32_SFLOAT;
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
}
