module odradek.ui {
    requires com.formdev.flatlaf.extras;
    requires com.formdev.flatlaf;
    requires java.desktop;
    requires org.slf4j;

    opens sh.adelessfox.odradek.ui.laf to com.formdev.flatlaf;

    exports sh.adelessfox.odradek.ui.laf;
    exports sh.adelessfox.odradek.ui.tree;
    exports sh.adelessfox.odradek.ui.util;
    exports sh.adelessfox.odradek.ui;

    provides com.formdev.flatlaf.FlatDefaultsAddon
        with sh.adelessfox.odradek.ui.OdradekDefaultsAddon;
}
