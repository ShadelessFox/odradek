package sh.adelessfox.odradek.game.hfw.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import sh.adelessfox.odradek.game.hfw.rtti.HFW;

import java.io.IOException;

final class GGUUIDTypeAdapter extends TypeAdapter<HFW.GGUUID> {
    static final GGUUIDTypeAdapter INSTANCE = new GGUUIDTypeAdapter();

    private GGUUIDTypeAdapter() {
    }

    @Override
    public void write(JsonWriter out, HFW.GGUUID value) throws IOException {
        out.value(value.toDisplayString());
    }

    @Override
    public HFW.GGUUID read(JsonReader in) throws IOException {
        throw new IOException("not implemented");
    }
}
