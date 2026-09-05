package sh.adelessfox.odradek.app.ui.settings.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;

public final class OptionalAdapterFactory implements TypeAdapterFactory {
    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        var rawType = typeToken.getRawType();
        if (!Optional.class.isAssignableFrom(rawType)) {
            return null;
        }

        var settingType = ((ParameterizedType) typeToken.getType()).getActualTypeArguments()[0];
        var settingTypeAdapter = gson.getAdapter(TypeToken.get(settingType));

        @SuppressWarnings("rawtypes")
        var adapter = new Adapter(settingTypeAdapter);

        return adapter;
    }

    private static final class Adapter<E> extends TypeAdapter<Optional<E>> {
        private final TypeAdapter<E> elementTypeAdapter;

        Adapter(TypeAdapter<E> elementTypeAdapter) {
            this.elementTypeAdapter = elementTypeAdapter;
        }

        @Override
        public void write(JsonWriter out, Optional<E> value) throws IOException {
            var inner = value.orElse(null);
            if (inner == null) {
                out.nullValue();
            } else {
                elementTypeAdapter.write(out, inner);
            }
        }

        @Override
        public Optional<E> read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return Optional.empty();
            }
            return Optional.ofNullable(elementTypeAdapter.read(in));
        }
    }
}
