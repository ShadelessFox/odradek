package sh.adelessfox.odradek.app.ui.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.ui.settings.gson.ObjectIdTypeAdapter;
import sh.adelessfox.odradek.app.ui.settings.gson.PathTypeAdapter;
import sh.adelessfox.odradek.app.ui.settings.gson.SettingAdapterFactory;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.game.ObjectId;
import sh.adelessfox.odradek.util.OperatingSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class SettingsManager {
    private static final Logger log = LoggerFactory.getLogger(SettingsManager.class);
    private static final Gson gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(Path.class, new PathTypeAdapter().nullSafe())
        .registerTypeHierarchyAdapter(ObjectId.class, new ObjectIdTypeAdapter().nullSafe())
        .registerTypeAdapterFactory(new SettingAdapterFactory())
        .setPrettyPrinting()
        .create();

    private final String identifier;
    private final EventBus eventBus;
    private final Settings settings;

    SettingsManager(String identifier, EventBus eventBus) {
        this.identifier = identifier;
        this.eventBus = eventBus;
        this.settings = load().orElseGet(Settings::new);

        eventBus.publish(new SettingsEvent.AfterLoad(settings));
        Runtime.getRuntime().addShutdownHook(new Thread(this::save));
    }

    Settings get() {
        return settings;
    }

    private Optional<Settings> load() {
        Path path = determinePath();
        if (!Files.exists(path)) {
            return Optional.empty();
        }

        try {
            return Optional.of(gson.fromJson(Files.readString(path), Settings.class));
        } catch (IOException | JsonParseException e) {
            log.error("Error while loading settings", e);
            return Optional.empty();
        }
    }

    private void save() {
        try {
            eventBus.publish(new SettingsEvent.BeforeSave(settings));
            Files.createDirectories(determinePath().getParent());
            Files.writeString(determinePath(), gson.toJson(settings));
        } catch (IOException e) {
            log.error("Error while saving settings", e);
        }
    }

    private Path determinePath() {
        String userHome = System.getProperty("user.home");
        if (userHome == null) {
            throw new IllegalStateException("Unable to determine user home directory");
        }
        return switch (OperatingSystem.current()) {
            case WINDOWS -> Path.of(userHome, "AppData", "Local", identifier, "settings.json");
            case MACOS -> Path.of(userHome, "Library", "Application Support", identifier, "settings.json");
            case LINUX -> Path.of(userHome, ".config", identifier, "settings.json");
        };
    }
}
