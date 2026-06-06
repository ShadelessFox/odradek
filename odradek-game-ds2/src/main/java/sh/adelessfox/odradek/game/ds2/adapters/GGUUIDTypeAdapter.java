package sh.adelessfox.odradek.game.ds2.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;

import java.io.IOException;

final class GGUUIDTypeAdapter extends TypeAdapter<DS2.GGUUID> {
    static final GGUUIDTypeAdapter INSTANCE = new GGUUIDTypeAdapter();

    private GGUUIDTypeAdapter() {
    }

    @Override
    public void write(JsonWriter out, DS2.GGUUID value) throws IOException {
        out.value(value.toDisplayString());
    }

    @Override
    public DS2.GGUUID read(JsonReader in) throws IOException {
        throw new IOException("not implemented");
    }
}
