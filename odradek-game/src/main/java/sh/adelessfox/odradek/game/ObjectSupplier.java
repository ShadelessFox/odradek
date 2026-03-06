package sh.adelessfox.odradek.game;

import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.data.TypedObject;

import java.io.IOException;

public interface ObjectSupplier extends ObjectIdHolder {
    TypedObject readObject(Game game) throws IOException;

    ClassTypeInfo objectType();
}
