package sh.adelessfox.odradek.app.ui.settings.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import sh.adelessfox.odradek.game.ObjectId;

import java.io.IOException;

public final class ObjectIdTypeAdapter extends TypeAdapter<ObjectId> {
    @Override
    public void write(JsonWriter out, ObjectId value) throws IOException {
        out.value(value.toString());
    }

    @Override
    public ObjectId read(JsonReader in) throws IOException {
        return ObjectId.valueOf(in.nextString());
    }
}
