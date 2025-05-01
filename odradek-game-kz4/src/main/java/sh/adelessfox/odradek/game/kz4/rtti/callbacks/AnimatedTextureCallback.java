package sh.adelessfox.odradek.game.kz4.rtti.callbacks;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class AnimatedTextureCallback implements ExtraBinaryDataCallback<AnimatedTextureCallback.AnimatedTextureData> {
    public interface AnimatedTextureData {
    }

    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, AnimatedTextureData object) throws IOException {
    }
}
