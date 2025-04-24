package sh.adelessfox.odradek.rtti.data;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public interface ExtraBinaryDataCallback<T> {
    void deserialize(BinaryReader reader, TypeFactory factory, T object) throws IOException;
}
