package sh.adelessfox.odradek.game.untildawn.rtti.callbacks;

import sh.adelessfox.odradek.game.untildawn.rtti.UntilDawn.Pose;
import sh.adelessfox.odradek.game.untildawn.rtti.UntilDawn.ProjMatrix;
import sh.adelessfox.odradek.game.untildawn.rtti.UntilDawn.TransMatrix;
import sh.adelessfox.odradek.game.untildawn.rtti.UntilDawnTypeReader;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class PoseCallback implements ExtraBinaryDataCallback<Pose> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, Pose object) throws IOException {
        if (reader.readByteBoolean()) {
            var count1 = reader.readInt();
            object.unk01(reader.readObjects(count1, r -> UntilDawnTypeReader.readCompound(TransMatrix.class, r, factory)));
            object.unk02(reader.readObjects(count1, r -> UntilDawnTypeReader.readCompound(ProjMatrix.class, r, factory)));

            var count2 = reader.readInt();
            object.unk03(reader.readFloats(count2));
        }
    }
}
