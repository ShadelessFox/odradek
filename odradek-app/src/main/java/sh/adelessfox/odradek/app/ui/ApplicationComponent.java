package sh.adelessfox.odradek.app.ui;

import dagger.BindsInstance;
import dagger.Component;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.app.ui.component.main.MainPresenter;
import sh.adelessfox.odradek.app.ui.settings.Settings;
import sh.adelessfox.odradek.app.ui.settings.SettingsModule;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;

@Singleton
@Component(modules = {ApplicationModule.class, SettingsModule.class})
interface ApplicationComponent {
    MainPresenter presenter();

    Settings settings();

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder game(ForbiddenWestGame game);

        @SuppressWarnings("ClassEscapesDefinedScope")
        ApplicationComponent build();
    }
}
