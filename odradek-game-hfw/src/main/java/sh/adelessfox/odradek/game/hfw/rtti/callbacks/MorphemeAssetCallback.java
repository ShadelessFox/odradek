package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class MorphemeAssetCallback implements ExtraBinaryDataCallback<HFW.MorphemeAsset> {
    @Override
    public void deserialize(BinarySource source, TypeFactory factory, HFW.MorphemeAsset object) throws IOException {
        object.data(source.readBytes(source.readInt()));
        object.unk01(source.readInt());
        object.unk02(source.readInt());
        object.unk03(source.readLong());
    }
}
