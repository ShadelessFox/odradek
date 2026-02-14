package sh.adelessfox.odradek.game.untildawn.rtti.callbacks;

import sh.adelessfox.odradek.game.untildawn.rtti.UntilDawn.EIndexFormat;
import sh.adelessfox.odradek.game.untildawn.rtti.UntilDawn.IndexArrayResource;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class IndexArrayResourceCallback implements ExtraBinaryDataCallback<IndexArrayResource> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, IndexArrayResource object) throws IOException {
        reader.align(4);
        object.count(reader.readInt());
        if (object.count() != 0) {
            object.flags(reader.readInt());
            object.format(EIndexFormat.valueOf(reader.readInt()));
            object.data(reader.readBytes(object.count() * object.format().unwrap().stride()));
            reader.align(4);
        }
    }
}
