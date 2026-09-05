package sh.adelessfox.odradek.settings.gson;

import com.google.gson.GsonBuilder;

public interface GsonAdapterProvider {
    void configure(GsonBuilder builder);

    int order();
}
