package sh.adelessfox.odradek.app.component.main;

import com.formdev.flatlaf.extras.components.FlatTabbedPane;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.app.component.common.View;
import sh.adelessfox.odradek.app.component.graph.GraphView;
import sh.adelessfox.odradek.app.menu.ActionIds;
import sh.adelessfox.odradek.ui.actions.Actions;
import sh.adelessfox.odradek.ui.components.view.ViewPanel;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

@Singleton
public class MainView implements View<JComponent> {
    private final ViewPanel root;
    private final FlatTabbedPane tabs;

    @Inject
    public MainView(GraphView graphView) {
        tabs = createTabsPane();

        root = new ViewPanel(ViewPanel.Placement.LEFT);
        root.addPrimaryView("Object graph", Fugue.getIcon("blue-document"), graphView);
        root.setContent(tabs);
        root.showView(graphView);

        Actions.installContextMenu(tabs, ActionIds.TABS_MENU_ID, key -> {
            if (DataKeys.COMPONENT.is(key)) {
                return Optional.of(tabs);
            }
            return Optional.empty();
        });
    }

    @Override
    public JComponent getRoot() {
        return root;
    }

    public FlatTabbedPane getTabs() {
        return tabs;
    }

    private FlatTabbedPane createTabsPane() {
        FlatTabbedPane tabs = new FlatTabbedPane();
        tabs.setTabPlacement(SwingConstants.TOP);
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.setTabsClosable(true);
        tabs.setTabCloseToolTipText("Close");
        tabs.setTabCloseCallback(JTabbedPane::remove);
        tabs.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!SwingUtilities.isMiddleMouseButton(e)) {
                    return;
                }
                int index = tabs.indexAtLocation(e.getX(), e.getY());
                if (index >= 0) {
                    tabs.remove(index);
                }
            }
        });

        tabs.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ctrl F4"), "closeTab");
        tabs.getActionMap().put("closeTab", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = tabs.getSelectedIndex();
                if (index >= 0) {
                    tabs.removeTabAt(index);
                }
            }
        });

        return tabs;
    }
}
