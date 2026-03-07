package sh.adelessfox.odradek.app.ui.settings;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.event.EventBus;

import java.nio.file.Path;

@Module
public interface SettingsModule {
    @Provides
    @Singleton
    static SettingsManager provideSettingsManager(@Named("config") Path config, EventBus eventBus) {
        return new SettingsManager(config.resolve("settings.json"), eventBus);
    }

    @Provides
    static Settings provideSettings(SettingsManager manager) {
        return manager.get();
    }
}
