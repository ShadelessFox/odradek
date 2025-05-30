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
import java.util.ArrayList;
import java.util.Optional;

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

        // TODO: Respect TextureSetParent
        // TODO: Respect StreamingMipOffsets

        var textureSet = texture.textureSetParent() != null ? texture.textureSetParent().get() : null;
        var streamingDataSource = textureSet != null ? textureSet.streamingDataSource() : texture.streamingDataSource();
        var streamedData = game.getStreamingSystem().getDataSourceData(streamingDataSource);
        var embeddedData = texture.data().embeddedData();

        int streamedDataOffset = 0;
        int embeddedDataOffset = 0;

        int width = Short.toUnsignedInt(texture.header().width());
        int height = Short.toUnsignedInt(texture.header().height());
        int numMips = Byte.toUnsignedInt(texture.header().numMips());

        var surfaces = new ArrayList<Surface>(numMips);
        for (int i = 0; i < numMips; i++) {
            int mipWidth = Math.max(width >> i, format.block().width());
            int mipHeight = Math.max(height >> i, format.block().height());
            int mipSize = Math.toIntExact((long) mipWidth * mipHeight * format.block().bitsPerPixel() / 8);
            var mipData = new byte[mipSize];

            if (i >= texture.data().streamedMips()) {
                System.arraycopy(embeddedData, embeddedDataOffset, mipData, 0, mipSize);
                embeddedDataOffset += mipSize;
            } else if (textureSet == null) {
                System.arraycopy(streamedData, streamedDataOffset, mipData, 0, mipSize);
                streamedDataOffset += mipSize;
            } else {
                System.arraycopy(streamedData, texture.streamingMipOffsets()[i], mipData, 0, mipSize);
            }

            surfaces.add(new Surface(mipWidth, mipHeight, mipData));
        }

        assert streamedDataOffset == streamedData.length || textureSet != null;
        assert embeddedDataOffset == embeddedData.length;

        return Optional.of(new Texture(format, type, surfaces, numMips));
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
            case _0 -> Optional.of(TextureType.SURFACE);
            // case _1 -> Optional.of(TextureType.VOLUME);
            // case _3 -> Optional.of(TextureType.ARRAY);
            // case CubeMap -> Optional.of(TextureType.CUBEMAP);
            default -> Optional.empty();
        };
    }
}
