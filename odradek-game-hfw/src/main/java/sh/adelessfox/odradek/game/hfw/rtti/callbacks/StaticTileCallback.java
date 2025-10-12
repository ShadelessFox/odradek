package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.Mat34;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.Mat44;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.StaticTile;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class StaticTileCallback implements ExtraBinaryDataCallback<StaticTile> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, StaticTile object) throws IOException {
        object.unk01(reader.readBytes(reader.readInt() * 20));
        object.unk02(reader.readObjects(reader.readInt(), r -> HFWTypeReader.readCompound(Mat44.class, r, factory)));
        object.unk03(reader.readBytes(reader.readInt() * 12));
        object.unk04(reader.readBytes(reader.readInt() * 16));
        object.unk05(reader.readObjects(reader.readInt(), r -> HFWTypeReader.readCompound(Mat34.class, r, factory)));
    }
}
