package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class StaticTileCallback implements ExtraBinaryDataCallback<HFW.StaticTile> {
    @Override
    public void deserialize(BinarySource source, TypeFactory factory, HFW.StaticTile object) throws IOException {
        object.unk01(source.readBytes(source.readInt() * 20));
        object.unk02(source.readBytes(source.readInt() * 64));
        object.unk03(source.readBytes(source.readInt() * 12));
        object.unk04(source.readBytes(source.readInt() * 16));
        object.unk05(source.readObjects(source.readInt(), r -> HFWTypeReader.readCompound(HFW.Mat34.class, r, factory)));
    }
}
