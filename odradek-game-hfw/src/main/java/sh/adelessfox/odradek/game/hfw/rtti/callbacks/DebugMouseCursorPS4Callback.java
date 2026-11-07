package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class DebugMouseCursorPS4Callback implements ExtraBinaryDataCallback<HFW.DebugMouseCursorPS4> {
    @Override
    public void deserialize(BinarySource source, TypeFactory factory, HFW.DebugMouseCursorPS4 object) throws IOException {
        object.stride(source.readInt());
        object.data(source.readBytes(source.readInt()));
    }
}
