package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class MorphemeNetworkDefResourceCallback implements ExtraBinaryDataCallback<HFW.MorphemeNetworkDefResource> {
    @Override
    public void deserialize(BinarySource source, TypeFactory factory, HFW.MorphemeNetworkDefResource object) throws IOException {
        // does nothing
    }
}
