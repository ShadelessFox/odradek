package sh.adelessfox.odradek.app.ui;

import sh.adelessfox.odradek.app.ui.component.main.MainPresenter;
import sh.adelessfox.odradek.ui.data.DataKey;

public final class ApplicationKeys {
    private ApplicationKeys() {
    }

    public static final DataKey<MainPresenter> MAIN_PRESENTER = DataKey.create("main presenter");
    public static final DataKey<Boolean> DEBUG_MODE = DataKey.create("debug mode");
}
