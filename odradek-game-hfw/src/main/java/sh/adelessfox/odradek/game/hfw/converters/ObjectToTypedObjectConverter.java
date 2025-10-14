package sh.adelessfox.odradek.game.hfw.converters;

import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.rtti.runtime.TypedObject;

import java.util.Optional;
import java.util.Set;

public class ObjectToTypedObjectConverter implements Converter<ForbiddenWestGame, TypedObject> {
    @Override
    public Optional<TypedObject> convert(Object object, ForbiddenWestGame game) {
        return Optional.of((TypedObject) object);
    }

    @Override
    public Set<Class<?>> supportedTypes() {
        return Set.of(TypedObject.class);
    }
}
