package sh.adelessfox.odradek.ui.data;

import javax.swing.*;

public final class DataKeys {
    private DataKeys() {
    }

    public static final DataKey<JComponent> COMPONENT = DataKey.create("component");
    public static final DataKey<Object> SELECTION = DataKey.create("selection");
}
