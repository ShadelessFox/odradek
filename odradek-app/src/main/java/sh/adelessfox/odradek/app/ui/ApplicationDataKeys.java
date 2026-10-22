package sh.adelessfox.odradek.app.ui;

import sh.adelessfox.odradek.data.DataKey;
import sh.adelessfox.odradek.settings.Settings;

import java.nio.file.Path;

/** Shared application services not owned by the generic UI module. */
public final class ApplicationDataKeys {
    public static final DataKey<Settings> SETTINGS = DataKey.of(Settings.class);
    public static final DataKey<Path> CONFIG_PATH = DataKey.of(Path.class).named("config");

    private ApplicationDataKeys() {
    }
}
