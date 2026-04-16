package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class MorphemeAnimationCallback implements ExtraBinaryDataCallback<HFW.MorphemeAnimation> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, HFW.MorphemeAnimation object) throws IOException {
        object.data(reader.readBytes(reader.readInt()));
        object.hash(HFWTypeReader.readCompound(HFW.MurmurHashValue.class, reader, factory));
    }
}
