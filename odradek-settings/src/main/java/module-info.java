module odradek.settings {
    requires com.google.gson;
    requires odradek.core;
    requires org.slf4j;

    exports sh.adelessfox.odradek.settings;
    exports sh.adelessfox.odradek.settings.gson;

    uses sh.adelessfox.odradek.settings.gson.GsonAdapterProvider;
}
