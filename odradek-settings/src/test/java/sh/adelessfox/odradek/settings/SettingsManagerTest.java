package sh.adelessfox.odradek.settings;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sh.adelessfox.odradek.event.DefaultEventBus;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SettingsManagerTest {
    @Test
    void readsAndWritesTypedSettingsWithoutDiscardingUnknownValues(@TempDir Path directory) throws Exception {
        var path = directory.resolve("settings.json");
        Files.writeString(path, """
            {
              "known": "before",
              "unknown": {"value": 42}
            }
            """);

        var manager = new SettingsManager(path, new DefaultEventBus(), false);
        var key = SettingsKey.of("known", String.class, () -> "default");

        assertEquals("before", manager.get(key).value());
        manager.get(key).set("after");
        manager.save();

        var document = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        assertEquals("after", document.get("known").getAsString());
        assertEquals(42, document.getAsJsonObject("unknown").get("value").getAsInt());
    }

    @Test
    void returnsTheDefaultForMissingSettings(@TempDir Path directory) {
        var manager = new SettingsManager(
            directory.resolve("settings.json"),
            new DefaultEventBus(),
            false);

        var key = SettingsKey.of("missing", Integer.class, () -> 42);

        assertEquals(42, manager.get(key).value());
    }
}
