package sh.adelessfox.odradek.game.ds2.adapters;

import com.google.gson.TypeAdapter;
import sh.adelessfox.odradek.export.json.spi.TypeInfoAdapterFactory;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.rtti.TypeInfo;

import java.util.Optional;

public final class DS2TypeInfoAdapterFactory implements TypeInfoAdapterFactory {
    @Override
    public Optional<TypeAdapter<?>> create(TypeInfo info) {
        if (DS2.GGUUID.class.isAssignableFrom(info.type())) {
            return Optional.of(GGUUIDTypeAdapter.INSTANCE);
        } else {
            return Optional.empty();
        }
    }
}
