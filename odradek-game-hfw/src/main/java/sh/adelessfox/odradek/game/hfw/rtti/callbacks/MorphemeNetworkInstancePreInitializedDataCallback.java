package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class MorphemeNetworkInstancePreInitializedDataCallback implements ExtraBinaryDataCallback<HFW.MorphemeNetworkInstancePreInitializedData> {
    @Override
    public void deserialize(BinarySource source, TypeFactory factory, HFW.MorphemeNetworkInstancePreInitializedData object) throws IOException {
        var count = source.readInt();
        var unk04 = source.readInt();
        if (unk04 != 4) {
            throw new IOException("Value expected to be 4, was " + unk04);
        }
        if (count > 0) {
            object.unk01(source.readBytes(count));
            object.unk02(source.readInts(source.readInt()));
        }
    }
}
