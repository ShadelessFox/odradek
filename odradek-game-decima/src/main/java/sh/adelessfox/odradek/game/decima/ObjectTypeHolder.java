package sh.adelessfox.odradek.game.decima;

import sh.adelessfox.odradek.rtti.ClassTypeInfo;

public interface ObjectTypeHolder extends ObjectIdHolder {
    ClassTypeInfo objectType();

    @Override
    default ClassTypeInfo objectType(DecimaGame game) {
        return objectType();
    }
}
