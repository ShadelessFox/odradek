package sh.adelessfox.odradek.game.hfw.converters.audio;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.SimpleSoundResource;

import java.util.Optional;

public class SimpleSoundResourceToAudioConverter
    extends BaseAudioConverter<SimpleSoundResource>
    implements Converter<SimpleSoundResource, Audio, ForbiddenWestGame> {

    @Override
    public Optional<Audio> convert(SimpleSoundResource object, ForbiddenWestGame game) {
        var resource = object.sound().wave();
        if (resource != null) {
            return Converter.convert(resource.get(), Audio.class, game);
        }
        return Optional.empty();
    }
}
