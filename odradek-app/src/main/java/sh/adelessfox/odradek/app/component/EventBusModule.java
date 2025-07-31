package sh.adelessfox.odradek.app.component;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.event.DefaultEventBus;
import sh.adelessfox.odradek.event.EventBus;

@Module
public interface EventBusModule {
    @Provides
    @Singleton
    static EventBus provideEventBus() {
        return new DefaultEventBus();
    }
}
