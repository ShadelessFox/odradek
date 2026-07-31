package sh.adelessfox.odradek.app.ui.settings.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.OptionalInt;

public final class OptionalIntTypeAdapter extends TypeAdapter<OptionalInt> {
    @Override
    public void write(JsonWriter out, OptionalInt value) throws IOException {
        if (value.isEmpty()) {
            out.nullValue();
        } else {
            out.value(value.getAsInt());
        }
    }

    @Override
    public OptionalInt read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return OptionalInt.empty();
        } else {
            return OptionalInt.of(in.nextInt());
        }
    }
}
