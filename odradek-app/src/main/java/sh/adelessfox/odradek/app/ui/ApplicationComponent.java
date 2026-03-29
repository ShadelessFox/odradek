package sh.adelessfox.odradek.app.ui;

import dagger.BindsInstance;
import dagger.Component;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.app.ui.bookmarks.Bookmarks;
import sh.adelessfox.odradek.app.ui.component.main.MainPresenter;
import sh.adelessfox.odradek.app.ui.settings.Settings;
import sh.adelessfox.odradek.app.ui.settings.SettingsModule;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.ui.editors.EditorManager;

import java.nio.file.Path;

@Singleton
@Component(modules = {ApplicationModule.class, SettingsModule.class})
interface ApplicationComponent {
    MainPresenter presenter();

    EditorManager editors();

    Settings settings();

    Bookmarks bookmarks();

    EventBus events();

    Game game();

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder game(Game game);

        @BindsInstance
        Builder config(@Named("config") Path config);

        @SuppressWarnings("ClassEscapesDefinedScope")
        ApplicationComponent build();
    }
}
