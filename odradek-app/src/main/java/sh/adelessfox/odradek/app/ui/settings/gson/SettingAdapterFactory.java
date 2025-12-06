package sh.adelessfox.odradek.app.ui.settings.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import sh.adelessfox.odradek.app.ui.settings.Setting;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

public final class SettingAdapterFactory implements TypeAdapterFactory {
    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        var rawType = typeToken.getRawType();
        if (!Setting.class.isAssignableFrom(rawType)) {
            return null;
        }

        var settingType = ((ParameterizedType) typeToken.getType()).getActualTypeArguments()[0];
        var settingTypeAdapter = gson.getAdapter(TypeToken.get(settingType));

        @SuppressWarnings("rawtypes")
        var adapter = new Adapter(settingTypeAdapter);

        return adapter;
    }

    private static final class Adapter<E> extends TypeAdapter<Setting<E>> {
        private final TypeAdapter<E> elementTypeAdapter;

        Adapter(TypeAdapter<E> elementTypeAdapter) {
            this.elementTypeAdapter = elementTypeAdapter;
        }

        @Override
        public void write(JsonWriter out, Setting<E> value) throws IOException {
            if (value.get().isEmpty()) {
                out.nullValue();
                return;
            }
            elementTypeAdapter.write(out, value.get().get());
        }

        @Override
        public Setting<E> read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return new Setting<>();
            }
            return new Setting<>(elementTypeAdapter.read(in));
        }
    }
}
