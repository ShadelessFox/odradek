package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.Pose;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

import static sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.Mat34;

public class PoseCallback implements ExtraBinaryDataCallback<Pose> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, Pose object) throws IOException {
        if (reader.readByteBoolean()) {
            var count1 = reader.readInt();
            object.unk01(reader.readObjects(count1, r -> HFWTypeReader.readCompound(Mat34.class, r, factory)));
            object.unk02(reader.readObjects(count1, r -> HFWTypeReader.readCompound(Mat34.class, r, factory)));

            var count2 = reader.readInt();
            object.unk03(reader.readInts(count2));
        }
    }
}
