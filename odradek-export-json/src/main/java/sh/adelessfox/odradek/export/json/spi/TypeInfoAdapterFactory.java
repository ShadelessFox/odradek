package sh.adelessfox.odradek.export.json.spi;

import com.google.gson.TypeAdapter;
import sh.adelessfox.odradek.rtti.TypeInfo;

import java.util.Optional;

/**
 * Factory for creating {@link TypeAdapter type adapters} based on {@link TypeInfo}.
 *
 * @see com.google.gson.TypeAdapterFactory
 */
public interface TypeInfoAdapterFactory {
    /**
     * Creates a {@link TypeAdapter} for the given {@link TypeInfo}.
     *
     * @param info type info
     * @return an optional containing the created type adapter,
     * or an empty optional if this factory does not support the given type info
     */
    Optional<TypeAdapter<?>> create(TypeInfo info);
}
