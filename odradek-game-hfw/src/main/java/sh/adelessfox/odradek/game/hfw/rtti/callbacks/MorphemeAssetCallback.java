package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class MorphemeAssetCallback implements ExtraBinaryDataCallback<HFW.MorphemeAsset> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, HFW.MorphemeAsset object) throws IOException {
        object.data(reader.readBytes(reader.readInt()));
        object.unk01(reader.readInt());
        object.unk02(reader.readInt());
        object.unk03(reader.readLong());
    }
}
