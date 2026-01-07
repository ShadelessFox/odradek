package sh.adelessfox.odradek.app.ui.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.ui.settings.gson.ObjectIdTypeAdapter;
import sh.adelessfox.odradek.app.ui.settings.gson.PathTypeAdapter;
import sh.adelessfox.odradek.app.ui.settings.gson.SettingAdapterFactory;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.game.ObjectId;
import sh.adelessfox.odradek.util.OS;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class SettingsManager {
    private static final Logger log = LoggerFactory.getLogger(SettingsManager.class);
    private static final Gson gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(Path.class, new PathTypeAdapter().nullSafe())
        .registerTypeHierarchyAdapter(ObjectId.class, new ObjectIdTypeAdapter().nullSafe())
        .registerTypeAdapterFactory(new SettingAdapterFactory())
        .setPrettyPrinting()
        .create();

    private final Path path;
    private final EventBus eventBus;
    private final Settings settings;

    SettingsManager(String identifier, EventBus eventBus) {
        this.path = determinePath(identifier);
        this.eventBus = eventBus;
        this.settings = load(path).orElseGet(Settings::new);

        eventBus.publish(new SettingsEvent.AfterLoad(settings));

        // noinspection resource
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setName("Odradek Settings Saver");
            return thread;
        }).scheduleAtFixedRate(this::save, 5, 5, TimeUnit.MINUTES);

        Runtime.getRuntime().addShutdownHook(new Thread(this::save));
    }

    Settings get() {
        return settings;
    }

    private static Optional<Settings> load(Path path) {
        if (!Files.exists(path)) {
            return Optional.empty();
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return Optional.of(gson.fromJson(reader, Settings.class));
        } catch (Exception e) {
            log.error("Error while loading settings", e);
            return Optional.empty();
        }
    }

    private void save() {
        eventBus.publish(new SettingsEvent.BeforeSave(settings));

        try {
            Files.createDirectories(path.getParent());
            if (Files.exists(path)) {
                Files.copy(path, path.resolveSibling(path.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                gson.toJson(settings, writer);
            }
        } catch (IOException e) {
            log.error("Error while saving settings", e);
        }
    }

    private static Path determinePath(String identifier) {
        String userHome = System.getProperty("user.home");
        if (userHome == null) {
            throw new IllegalStateException("Unable to determine user home directory");
        }
        return switch (OS.name()) {
            case WINDOWS -> Path.of(userHome, "AppData", "Local", identifier, "settings.json");
            case MACOS -> Path.of(userHome, "Library", "Application Support", identifier, "settings.json");
            case LINUX -> Path.of(userHome, ".config", identifier, "settings.json");
        };
    }
}
