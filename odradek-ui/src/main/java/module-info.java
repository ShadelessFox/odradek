module odradek.ui {
    requires static odradek.rtti;

    requires com.formdev.flatlaf.extras;
    requires com.formdev.flatlaf;
    requires com.miglayout.swing;
    requires java.desktop;
    requires odradek.core;
    requires org.slf4j;

    opens sh.adelessfox.odradek.ui.components.laf to com.formdev.flatlaf;

    exports sh.adelessfox.odradek.ui.actions;
    exports sh.adelessfox.odradek.ui.components;
    exports sh.adelessfox.odradek.ui.data;
    exports sh.adelessfox.odradek.ui.components.laf;
    exports sh.adelessfox.odradek.ui.components.tree;
    exports sh.adelessfox.odradek.ui.components.toolwindow;
    exports sh.adelessfox.odradek.ui.util;
    exports sh.adelessfox.odradek.ui;

    uses sh.adelessfox.odradek.ui.Renderer;
    uses sh.adelessfox.odradek.ui.Viewer;
    uses sh.adelessfox.odradek.ui.actions.Action;

    provides com.formdev.flatlaf.FlatDefaultsAddon with
        sh.adelessfox.odradek.ui.components.laf.OdradekDefaultsAddon;

    provides sh.adelessfox.odradek.ui.Renderer with
        sh.adelessfox.odradek.ui.renderers.ContainerRenderer,
        sh.adelessfox.odradek.ui.renderers.PointerRenderer;
}
