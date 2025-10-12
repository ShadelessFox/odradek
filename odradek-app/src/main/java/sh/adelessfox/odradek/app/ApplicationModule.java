package sh.adelessfox.odradek.app;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.event.DefaultEventBus;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.ui.editors.EditorManager;
import sh.adelessfox.odradek.ui.editors.stack.EditorStackManager;

@Module
interface ApplicationModule {
    @Provides
    @Singleton
    static EditorManager provideEditorManager() {
        return new EditorStackManager();
    }

    @Provides
    @Singleton
    static EventBus provideEventBus() {
        return new DefaultEventBus();
    }
}
