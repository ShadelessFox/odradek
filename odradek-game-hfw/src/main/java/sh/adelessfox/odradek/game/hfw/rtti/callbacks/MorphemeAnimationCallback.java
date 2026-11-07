package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class MorphemeAnimationCallback implements ExtraBinaryDataCallback<HFW.MorphemeAnimation> {
    @Override
    public void deserialize(BinarySource source, TypeFactory factory, HFW.MorphemeAnimation object) throws IOException {
        object.data(source.readBytes(source.readInt()));
        object.hash(HFWTypeReader.readCompound(HFW.MurmurHashValue.class, source, factory));
    }
}
