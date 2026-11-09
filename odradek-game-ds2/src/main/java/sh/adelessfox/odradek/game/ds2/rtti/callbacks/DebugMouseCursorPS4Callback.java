package sh.adelessfox.odradek.game.ds2.rtti.callbacks;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class DebugMouseCursorPS4Callback implements ExtraBinaryDataCallback<DS2.DebugMouseCursorPS4> {
    @Override
    public void deserialize(BinarySource source, TypeFactory factory, DS2.DebugMouseCursorPS4 object) throws IOException {
        object.stride(source.readInt());
        object.data(source.readBytes(source.readInt()));
    }
}
