package sh.adelessfox.odradek.game.hfw.converters.texture;

import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.HFWGame;
import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.texture.Texture;

import java.util.Optional;

public final class TextureBindingWithHandleToTextureConverter
    extends BaseTextureConverter<HFW.TextureBindingWithHandle>
    implements Converter<HFW.TextureBindingWithHandle, Texture, HFWGame> {

    @Override
    public Optional<Texture> convert(HFW.TextureBindingWithHandle object, HFWGame game) {
        var resource = object.textureResource().get();
        if (resource != null) {
            return Converter.convert(resource, Texture.class, game);
        }
        return Optional.empty();
    }
}
