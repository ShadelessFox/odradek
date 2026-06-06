package sh.adelessfox.odradek.game.hfw.adapters;

import com.google.gson.TypeAdapter;
import sh.adelessfox.odradek.export.json.spi.TypeInfoAdapterFactory;
import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.rtti.TypeInfo;

import java.util.Optional;

public final class HFWTypeInfoAdapterFactory implements TypeInfoAdapterFactory {
    @Override
    public Optional<TypeAdapter<?>> create(TypeInfo info) {
        if (HFW.GGUUID.class.isAssignableFrom(info.type())) {
            return Optional.of(GGUUIDTypeAdapter.INSTANCE);
        } else {
            return Optional.empty();
        }
    }
}
