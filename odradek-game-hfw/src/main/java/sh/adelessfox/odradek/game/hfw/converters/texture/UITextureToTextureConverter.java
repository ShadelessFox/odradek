package sh.adelessfox.odradek.game.hfw.converters.texture;

import sh.adelessfox.odradek.compression.Decompressor;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest;
import sh.adelessfox.odradek.texture.Surface;
import sh.adelessfox.odradek.texture.Texture;
import sh.adelessfox.odradek.texture.TextureColorSpace;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;

public final class UITextureToTextureConverter
    extends BaseTextureConverter<HorizonForbiddenWest.UITexture>
    implements Converter<HorizonForbiddenWest.UITexture, Texture, ForbiddenWestGame> {

    @Override
    public Optional<Texture> convert(HorizonForbiddenWest.UITexture object, ForbiddenWestGame game) {
        if (object.animated()) {
            return convertTextureFrames(object.largeTextureFrames());
        } else {
            return convertTexture(object.largeTexture());
        }
    }

    private static Optional<Texture> convertTextureFrames(HorizonForbiddenWest.UITextureFramesInfo object) {
        var format = mapFormat(object.pixelFormat().unwrap()).orElse(null);
        if (format == null) {
            return Optional.empty();
        }

        var spans = object.spans();
        var data = object.data();

        var surfaces = new ArrayList<Surface>(spans.length);
        for (long span : spans) {
            int offset = (int) (span);
            int length = (int) (span >>> 32);
            var buffer = new byte[object.size()];

            Decompressor.lz4().decompress(data, offset, length, buffer, 0, object.size());
            surfaces.add(new Surface(object.width(), object.height(), buffer));
        }

        return Optional.of(Texture.ofAnimated2D(
            format,
            TextureColorSpace.SRGB, // hardcoded in UITextureFrames::UITextureFrames
            surfaces,
            spans.length,
            mapFrequency(object.frequency().unwrap())
        ));
    }

    private static Optional<Texture> convertTexture(HorizonForbiddenWest.TextureInfo object) {
        var format = mapFormat(object.header().pixelFormat().unwrap()).orElse(null);
        if (format == null) {
            return Optional.empty();
        }

        assert object.header().type() == HorizonForbiddenWest.ETextureType._2D;
        assert object.header().numMips() == 1;
        assert object.header().numSurfaces() == 0;
        assert object.data().streamedMips() == 0;

        int width = object.header().width() & 0x3FFF;
        int height = object.header().height() & 0x3FFF;
        var surface = Surface.create(width, height, format, object.data().embeddedData());

        return Optional.of(Texture.of2D(
            format,
            mapColorSpace(object.header().colorSpace().unwrap()),
            surface
        ));
    }

    private static Duration mapFrequency(HorizonForbiddenWest.EUpdateFrequency frequency) {
        double hz = switch (frequency) {
            case _0 -> 7.49;
            case _1 -> 14.99;
            case _2 -> 29.97;
            case _3 -> 59.94;
            case _4 -> 119.88;
        };
        return Duration.ofNanos((long) (1_000_000_000 / hz));
    }
}
