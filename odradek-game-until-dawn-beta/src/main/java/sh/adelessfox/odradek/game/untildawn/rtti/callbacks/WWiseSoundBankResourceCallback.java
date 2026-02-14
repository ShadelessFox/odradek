package sh.adelessfox.odradek.game.untildawn.rtti.callbacks;

import sh.adelessfox.odradek.game.untildawn.rtti.UntilDawn.WWiseSoundBankResource;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class WWiseSoundBankResourceCallback implements ExtraBinaryDataCallback<WWiseSoundBankResource> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, WWiseSoundBankResource object) throws IOException {
        object.data(reader.readBytes(reader.readInt()));
    }
}
