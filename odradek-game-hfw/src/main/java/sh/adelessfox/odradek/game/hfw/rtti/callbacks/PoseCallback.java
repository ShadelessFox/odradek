package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;
import wtf.reversed.toolbox.io.BoolFormat;

import java.io.IOException;

public class PoseCallback implements ExtraBinaryDataCallback<HFW.Pose> {
    @Override
    public void deserialize(BinarySource source, TypeFactory factory, HFW.Pose object) throws IOException {
        if (source.readBool(BoolFormat.BYTE)) {
            var count1 = source.readInt();
            object.unk01(source.readObjects(count1, r -> HFWTypeReader.readCompound(HFW.Mat34.class, r, factory)));
            object.unk02(source.readObjects(count1, r -> HFWTypeReader.readCompound(HFW.Mat34.class, r, factory)));

            var count2 = source.readInt();
            object.unk03(source.readInts(count2));
        }
    }
}
