package sh.adelessfox.odradek.game.hfw.converters.texture;

import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest;
import sh.adelessfox.odradek.texture.Texture;

import java.util.Optional;

public final class TextureBindingWithHandleToTextureConverter
    extends BaseTextureConverter<HorizonForbiddenWest.TextureBindingWithHandle>
    implements Converter<HorizonForbiddenWest.TextureBindingWithHandle, Texture, ForbiddenWestGame> {

    @Override
    public Optional<Texture> convert(HorizonForbiddenWest.TextureBindingWithHandle object, ForbiddenWestGame game) {
        var resource = object.textureResource().get();
        if (resource != null) {
            return Converter.convert(resource, game, Texture.class);
        }
        return Optional.empty();
    }
}
