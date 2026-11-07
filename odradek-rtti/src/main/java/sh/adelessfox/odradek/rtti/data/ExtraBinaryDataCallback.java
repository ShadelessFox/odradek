package sh.adelessfox.odradek.rtti.data;

import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public interface ExtraBinaryDataCallback<T> {
    void deserialize(BinarySource source, TypeFactory factory, T object) throws IOException;
}
