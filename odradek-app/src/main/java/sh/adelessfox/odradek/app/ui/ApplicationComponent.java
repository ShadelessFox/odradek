package sh.adelessfox.odradek.app.ui;

import dagger.BindsInstance;
import dagger.Component;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.app.ui.bookmarks.Bookmarks;
import sh.adelessfox.odradek.app.ui.component.main.MainPresenter;
import sh.adelessfox.odradek.app.ui.settings.Settings;
import sh.adelessfox.odradek.app.ui.settings.SettingsModule;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.ui.editors.EditorManager;

@Singleton
@Component(modules = {ApplicationModule.class, SettingsModule.class})
interface ApplicationComponent {
    MainPresenter presenter();

    EditorManager editorManager();

    Settings settings();

    Bookmarks bookmarks();

    ForbiddenWestGame game();

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder game(ForbiddenWestGame game);

        @SuppressWarnings("ClassEscapesDefinedScope")
        ApplicationComponent build();
    }
}
