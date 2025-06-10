module odradek.ui {
    requires com.formdev.flatlaf.extras;
    requires com.formdev.flatlaf;
    requires java.desktop;
    requires org.slf4j;
    requires odradek.core;

    opens sh.adelessfox.odradek.ui.laf to com.formdev.flatlaf;

    exports sh.adelessfox.odradek.ui.actions;
    exports sh.adelessfox.odradek.ui.components;
    exports sh.adelessfox.odradek.ui.data;
    exports sh.adelessfox.odradek.ui.laf;
    exports sh.adelessfox.odradek.ui.tree;
    exports sh.adelessfox.odradek.ui.util;
    exports sh.adelessfox.odradek.ui;

    uses sh.adelessfox.odradek.ui.Viewer;
    uses sh.adelessfox.odradek.ui.actions.Action;

    provides com.formdev.flatlaf.FlatDefaultsAddon with
        sh.adelessfox.odradek.ui.laf.OdradekDefaultsAddon;
}
