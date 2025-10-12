package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.MorphemeAnimation;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.MurmurHashValue;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class MorphemeAnimationCallback implements ExtraBinaryDataCallback<MorphemeAnimation> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, MorphemeAnimation object) throws IOException {
        object.data(reader.readBytes(reader.readInt()));
        object.hash(HFWTypeReader.readCompound(MurmurHashValue.class, reader, factory));
    }
}
