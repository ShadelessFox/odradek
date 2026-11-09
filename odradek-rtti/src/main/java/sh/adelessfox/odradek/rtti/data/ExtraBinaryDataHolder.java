package sh.adelessfox.odradek.rtti.data;

import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public interface ExtraBinaryDataHolder {
    default void deserialize(BinarySource source, TypeFactory factory) throws IOException {
        throw new UnsupportedOperationException(
            "Missing callback for '"
            + getClass().getInterfaces()[0].getSimpleName()
            + "' required to read extra data at position " + source.position());
    }
}
