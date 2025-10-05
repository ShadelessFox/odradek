package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.MorphemeNetworkInstancePreInitializedData;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class MorphemeNetworkInstancePreInitializedDataCallback implements ExtraBinaryDataCallback<MorphemeNetworkInstancePreInitializedData> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, MorphemeNetworkInstancePreInitializedData object) throws IOException {
        var count = reader.readInt();
        var unk04 = reader.readInt();
        if (unk04 != 4) {
            throw new IOException("Value expected to be 4, was " + unk04);
        }
        if (count > 0) {
            object.unk01(reader.readBytes(count));
            object.unk02(reader.readInts(reader.readInt()));
        }
    }
}
