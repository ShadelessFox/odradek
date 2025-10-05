package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.DebugMouseCursorPS4;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class DebugMouseCursorPS4Callback implements ExtraBinaryDataCallback<DebugMouseCursorPS4> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, DebugMouseCursorPS4 object) throws IOException {
        object.stride(reader.readInt());
        object.data(reader.readBytes(reader.readInt()));
    }
}
