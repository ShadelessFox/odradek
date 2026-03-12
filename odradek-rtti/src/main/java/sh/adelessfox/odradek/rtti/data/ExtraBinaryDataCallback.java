package sh.adelessfox.odradek.rtti.data;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.io.BinaryWriter;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public interface ExtraBinaryDataCallback<T> {
    void deserialize(BinaryReader reader, TypeFactory factory, T object) throws IOException;

    default void serialize(BinaryWriter writer, T object) throws IOException {
        throw new UnsupportedOperationException(
            "Cannot serialize extra data of '"
                + object.getClass().getInterfaces()[0].getSimpleName()
                + "' at position " + writer.position() + " because it's not implemented");
    }
}
