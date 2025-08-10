package sh.adelessfox.odradek.app;

import sh.adelessfox.odradek.app.component.main.MainPresenter;
import sh.adelessfox.odradek.ui.data.DataKey;

public final class ApplicationKeys {
    private ApplicationKeys() {
    }

    public static final DataKey<MainPresenter> MAIN_PRESENTER = DataKey.create("main presenter");
}
