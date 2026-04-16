package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class MorphemeNetworkDefResourceCallback implements ExtraBinaryDataCallback<HFW.MorphemeNetworkDefResource> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, HFW.MorphemeNetworkDefResource object) throws IOException {
        // does nothing
    }
}
