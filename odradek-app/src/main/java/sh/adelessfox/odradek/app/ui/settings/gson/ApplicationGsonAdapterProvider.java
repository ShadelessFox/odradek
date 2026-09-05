package sh.adelessfox.odradek.app.ui.settings.gson;

import com.google.gson.GsonBuilder;
import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.settings.gson.GsonAdapterProvider;

import java.nio.file.Path;
import java.util.OptionalInt;

public final class ApplicationGsonAdapterProvider implements GsonAdapterProvider {
    @Override
    public void configure(GsonBuilder builder) {
        builder.registerTypeAdapterFactory(new OptionalAdapterFactory());
        builder.registerTypeHierarchyAdapter(Path.class, new PathTypeAdapter().nullSafe());
        builder.registerTypeAdapter(OptionalInt.class, new OptionalIntTypeAdapter());
        builder.registerTypeAdapter(ObjectId.class, new ObjectIdTypeAdapter().nullSafe());
    }

    @Override
    public int order() {
        return 0;
    }
}
