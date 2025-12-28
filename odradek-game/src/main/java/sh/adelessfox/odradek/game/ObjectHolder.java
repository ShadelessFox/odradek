package sh.adelessfox.odradek.game;

import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.runtime.TypedObject;

import java.io.IOException;

public interface ObjectHolder {
    TypedObject readObject(Game game) throws IOException;

    ClassTypeInfo objectType();

    String objectName();
}
