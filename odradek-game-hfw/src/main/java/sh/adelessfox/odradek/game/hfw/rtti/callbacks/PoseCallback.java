package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class PoseCallback implements ExtraBinaryDataCallback<HFW.Pose> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, HFW.Pose object) throws IOException {
        if (reader.readByteBoolean()) {
            var count1 = reader.readInt();
            object.unk01(reader.readObjects(count1, r -> HFWTypeReader.readCompound(HFW.Mat34.class, r, factory)));
            object.unk02(reader.readObjects(count1, r -> HFWTypeReader.readCompound(HFW.Mat34.class, r, factory)));

            var count2 = reader.readInt();
            object.unk03(reader.readInts(count2));
        }
    }
}
