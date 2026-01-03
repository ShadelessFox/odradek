package sh.adelessfox.odradek.game.hfw.converters.audio;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.WaveResource;

import java.util.Optional;

public class WaveResourceToAudioConverter
    extends BaseAudioConverter<WaveResource>
    implements Converter<WaveResource, Audio, ForbiddenWestGame> {

    @Override
    public Optional<Audio> convert(WaveResource object, ForbiddenWestGame game) {
        var encoding = object.format().encoding().unwrap();
        var data = object.format().isStreaming()
            ? game.readDataSourceUnchecked(object.data().streamingDataSource())
            : object.data().waveData();

        return convert(encoding, data);
    }
}
