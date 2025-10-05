package sh.adelessfox.odradek.rtti.data;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public interface ExtraBinaryDataHolder {
    default void deserialize(BinaryReader reader, TypeFactory factory) throws IOException {
        throw new UnsupportedOperationException(
            "Missing callback for '"
            + getClass().getInterfaces()[0].getSimpleName()
            + "' required to read extra data at position " + reader.position());
    }
}
