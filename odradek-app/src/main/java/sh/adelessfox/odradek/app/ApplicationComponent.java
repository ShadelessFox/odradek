package sh.adelessfox.odradek.app;

import dagger.BindsInstance;
import dagger.Component;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.app.component.main.MainPresenter;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;

@Singleton
@Component(modules = ApplicationModule.class)
interface ApplicationComponent {
    MainPresenter presenter();

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder game(ForbiddenWestGame game);

        @SuppressWarnings("ClassEscapesDefinedScope")
        ApplicationComponent build();
    }
}
