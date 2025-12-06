package sh.adelessfox.odradek.app.ui.settings;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.event.EventBus;

@Module
public interface SettingsModule {
    @Provides
    @Singleton
    static SettingsManager provideSettingsManager(EventBus eventBus) {
        return new SettingsManager("Odradek", eventBus);
    }

    @Provides
    static Settings provideSettings(SettingsManager manager) {
        return manager.get();
    }
}
