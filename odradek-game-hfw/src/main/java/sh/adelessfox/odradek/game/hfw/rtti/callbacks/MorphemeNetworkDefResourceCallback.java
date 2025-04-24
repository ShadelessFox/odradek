package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class MorphemeNetworkDefResourceCallback implements ExtraBinaryDataCallback<MorphemeNetworkDefResourceCallback.MorphemeNetworkDefData> {
    public interface MorphemeNetworkDefData {
    }

    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, MorphemeNetworkDefData object) throws IOException {

    }
}
