package sh.adelessfox.odradek.app.ui;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.data.DataContext;
import sh.adelessfox.odradek.event.DefaultEventBus;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.ui.editors.EditorManager;
import sh.adelessfox.odradek.ui.editors.stack.EditorStackManager;

@Module
interface ApplicationModule {
    @Provides
    @Singleton
    static EditorManager provideEditorManager(DataContext context) {
        return new EditorStackManager(context);
    }

    @Provides
    @Singleton
    static EventBus provideEventBus() {
        return new DefaultEventBus();
    }
}
