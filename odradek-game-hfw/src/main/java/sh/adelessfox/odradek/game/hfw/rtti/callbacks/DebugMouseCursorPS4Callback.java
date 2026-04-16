package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class DebugMouseCursorPS4Callback implements ExtraBinaryDataCallback<HFW.DebugMouseCursorPS4> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, HFW.DebugMouseCursorPS4 object) throws IOException {
        object.stride(reader.readInt());
        object.data(reader.readBytes(reader.readInt()));
    }
}
