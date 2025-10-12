package sh.adelessfox.odradek.app;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.event.DefaultEventBus;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.ui.editors.EditorManager;

@Module
interface ApplicationModule {
    @Provides
    static EditorManager provideEditorManager() {
        return EditorManager.sharedInstance();
    }

    @Provides
    @Singleton
    static EventBus provideEventBus() {
        return new DefaultEventBus();
    }
}
