package sh.adelessfox.odradek.app.ui;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.runtime.TypedObject;

import java.io.IOException;

public interface ObjectProvider {
    TypedObject readObject(Game game) throws IOException;

    ClassTypeInfo objectType();

    String objectName();
}
