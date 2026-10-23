package sh.adelessfox.odradek.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.settings.gson.GsonAdapterProvider;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages settings, providing methods to retrieve and modify settings values.
 * <p>
 * This class is thread-safe and supports automatic saving of settings to a specified file path.
 */
public final class SettingsManager implements Settings, AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(SettingsManager.class);

    private final Path path;
    private final EventBus eventBus;
    private final Gson gson;
    private final JsonObject document;
    private final Map<String, Entry<?>> entries = new HashMap<>();
    private ScheduledExecutorService autoSaveExecutor;
    private boolean closed;

    public SettingsManager(Path path, EventBus eventBus) {
        this(path, eventBus, true);
    }

    SettingsManager(Path path, EventBus eventBus, boolean autoSave) {
        this.path = path;
        this.eventBus = eventBus;
        this.gson = createGson();
        this.document = load(path, gson).orElseGet(JsonObject::new);

        try {
            eventBus.publish(new SettingsEvent.AfterLoad(this));

            synchronized (this) {
                // AfterLoad callbacks may close the manager before construction completes
                if (autoSave && !closed) {
                    autoSaveExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                        var thread = new Thread(r);
                        thread.setName("Odradek Settings Saver");
                        return thread;
                    });
                    autoSaveExecutor.scheduleAtFixedRate(this::save, 5, 5, TimeUnit.MINUTES);
                }
            }
        } catch (RuntimeException | Error e) {
            close();
            throw e;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T> Setting<T> get(SettingsKey<T> key) {
        var entry = entries.computeIfAbsent(key.name(), _ -> {
            var value = read(key);
            var setting = new MutableSetting<>(key, value);
            return new Entry<>(key, setting);
        });
        if (!entry.key.type().equals(key.type())) {
            throw new IllegalArgumentException(
                "Settings key '" + key.name() + "' is already registered with type " + entry.key.type());
        }
        return (Setting<T>) entry.setting;
    }

    private <T> T read(SettingsKey<T> key) {
        var json = document.get(key.name());
        if (json == null || json.isJsonNull()) {
            return key.createDefault();
        }
        try {
            return gson.fromJson(json, key.type());
        } catch (Exception e) {
            log.error("Error while reading setting '{}'", key.name(), e);
            return key.createDefault();
        }
    }

    private static Gson createGson() {
        var builder = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls();
        ServiceLoader.load(GsonAdapterProvider.class).stream()
            .map(ServiceLoader.Provider::get)
            .sorted(Comparator.comparingInt(GsonAdapterProvider::order))
            .forEach(provider -> provider.configure(builder));
        return builder.create();
    }

    private static Optional<JsonObject> load(Path path, Gson gson) {
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        try (var reader = Files.newBufferedReader(path)) {
            return Optional.ofNullable(gson.fromJson(reader, JsonObject.class));
        } catch (Exception e) {
            log.error("Error while loading settings", e);
            return Optional.empty();
        }
    }

    /**
     * Publishes {@link SettingsEvent.BeforeSave} and writes settings unless already closed.
     */
    public synchronized void save() {
        if (closed) {
            return;
        }
        eventBus.publish(new SettingsEvent.BeforeSave(this));
        if (closed) {
            return;
        }
        for (var entry : entries.values()) {
            document.add(entry.key.name(), entry.toJson(gson));
        }
        try {
            Files.createDirectories(path.getParent());
            if (Files.exists(path)) {
                Files.copy(path, path.resolveSibling(path.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                gson.toJson(document, writer);
            }
        } catch (IOException e) {
            log.error("Error while saving settings", e);
        }
    }

    /**
     * Stops automatic saving without saving settings. Call {@link #save()} before closing
     * when settings should be persisted. Repeated calls have no effect.
     */
    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (autoSaveExecutor != null) {
            // Do not wait for termination: a queued save may be waiting for this monitor.
            autoSaveExecutor.shutdownNow();
        }
    }

    private record Entry<T>(SettingsKey<T> key, MutableSetting<T> setting) {
        JsonElement toJson(Gson gson) {
            return gson.toJsonTree(setting.value(), key.type());
        }
    }

    private static final class MutableSetting<T> implements Setting<T> {
        private final SettingsKey<T> key;
        private T value;

        private MutableSetting(SettingsKey<T> key, T value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public synchronized T value() {
            return value;
        }

        @Override
        public synchronized void set(T value) {
            this.value = Objects.requireNonNull(value, "value");
        }

        @Override
        public synchronized void reset() {
            value = key.createDefault();
        }

        @Override
        public String toString() {
            return "MutableSetting[key=" + key + ", value=" + value + "]";
        }
    }
}
