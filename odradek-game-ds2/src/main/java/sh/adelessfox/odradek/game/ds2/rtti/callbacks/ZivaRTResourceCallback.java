package sh.adelessfox.odradek.game.ds2.rtti.callbacks;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public final class ZivaRTResourceCallback implements ExtraBinaryDataCallback<DS2.ZivaRTResource> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, DS2.ZivaRTResource object) throws IOException {
        object.data(reader.readBytes(reader.readInt()));
    }
}
