module odradek.ui {
    requires static odradek.rtti;

    requires com.formdev.flatlaf.extras;
    requires com.formdev.flatlaf;
    requires com.miglayout.swing;
    requires java.desktop;
    requires org.slf4j;
    requires odradek.core;

    opens sh.adelessfox.odradek.ui.components.laf to com.formdev.flatlaf;

    exports sh.adelessfox.odradek.ui.actions;
    exports sh.adelessfox.odradek.ui.components.laf;
    exports sh.adelessfox.odradek.ui.components.tree;
    exports sh.adelessfox.odradek.ui.components.toolwindow;
    exports sh.adelessfox.odradek.ui.components;
    exports sh.adelessfox.odradek.ui.data;
    exports sh.adelessfox.odradek.ui.editors.stack;
    exports sh.adelessfox.odradek.ui.editors;
    exports sh.adelessfox.odradek.ui.util;
    exports sh.adelessfox.odradek.ui;

    uses sh.adelessfox.odradek.ui.Renderer;
    uses sh.adelessfox.odradek.ui.Viewer;
    uses sh.adelessfox.odradek.ui.actions.Action;
    uses sh.adelessfox.odradek.ui.editors.EditorProvider;

    provides com.formdev.flatlaf.FlatDefaultsAddon with
        sh.adelessfox.odradek.ui.components.laf.OdradekDefaultsAddon;

    provides sh.adelessfox.odradek.ui.Renderer with
        sh.adelessfox.odradek.ui.renderers.ContainerRenderer,
        sh.adelessfox.odradek.ui.renderers.PointerRenderer;

    provides sh.adelessfox.odradek.ui.actions.Action with
        sh.adelessfox.odradek.ui.editors.actions.CloseActiveTabAction,
        sh.adelessfox.odradek.ui.editors.actions.CloseAllTabsAction,
        sh.adelessfox.odradek.ui.editors.actions.CloseOtherTabsAction,
        sh.adelessfox.odradek.ui.editors.actions.MoveToOppositeGroupAction,
        sh.adelessfox.odradek.ui.editors.actions.SplitAndMoveDownAction,
        sh.adelessfox.odradek.ui.editors.actions.SplitAndMoveRightAction;
}
