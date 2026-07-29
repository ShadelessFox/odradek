package sh.adelessfox.odradek.ui.components.tools.demo;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import sh.adelessfox.odradek.ui.components.tools.ToolManagerImpl;
import sh.adelessfox.odradek.ui.components.tools.ToolPanel;
import sh.adelessfox.odradek.ui.components.tools.ToolState;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;
import java.awt.*;
import java.util.EnumSet;

public class ToolManagerDemo {
    static void main() {
        FlatLightLaf.setup();
        FlatInspector.install("ctrl shift alt X");
        FlatUIDefaultsInspector.install("ctrl shift alt Y");

        ToolManagerImpl manager = new ToolManagerImpl();
        manager.setCenter(new JLabel("Center", SwingConstants.CENTER));
        // manager.addPanel(new GraphToolPanel.Provider());
        // manager.addPanel(new BookmarkToolPanel.Provider());
        // manager.movePanel("graph", ToolPanel.Placement.LEFT_TOP);
        // manager.movePanel("bookmark", ToolPanel.Placement.LEFT_BOTTOM);
        // manager.openPanel("graph");

        // manager.setAnchorWeights(ToolPanelAnchor.LEFT, new ToolPanelAnchorState.Weights(0.2f, 0.2f));
        // manager.setAnchorWeights(ToolPanelAnchor.RIGHT, new ToolPanelAnchorState.Weights(0.5f, 0.25f));
        // manager.setAnchorWeights(ToolPanelAnchor.RIGHT, new ToolPanelAnchorState.Weights(0.5f, 0.2f));

        for (ToolPanel.Placement.Anchor anchor : EnumSet.allOf(ToolPanel.Placement.Anchor.class)) {
            for (int i = 0; i < 6; i++) {
                String id = anchor.name() + "-" + i;
                manager.addPanel(new ToolPanel.Provider() {
                    @Override
                    public String id() {
                        return id;
                    }

                    @Override
                    public String name() {
                        return id;
                    }

                    @Override
                    public Icon icon() {
                        return Fugue.getIcon("blue-document-number-" + anchor.ordinal());
                    }

                    @Override
                    public ToolPanel create() {
                        return () -> new JButton(id);
                    }
                }, new ToolPanel.Placement(anchor, i < 3));
            }
        }

        var dumpLayout = new JButton("Dump layout");
        dumpLayout.addActionListener(_ -> {
            System.out.println(manager.getState());
        });

        var saved = new ToolState[1];
        var saveLayout = new JButton("Save layout");
        saveLayout.addActionListener(_ -> {
            saved[0] = manager.getState();
            System.out.println("Layout saved");
        });

        var loadLayout = new JButton("Load layout");
        loadLayout.addActionListener(_ -> {
            if (saved[0] != null) {
                manager.setState(saved[0]);
                System.out.println("Layout loaded");
            }
        });

        var initial = manager.getState();
        var restoreLayout = new JButton("Initial layout");
        restoreLayout.addActionListener(_ -> {
            manager.setState(initial);
            System.out.println("Layout restored");
        });

        var toggleLeftRight = new JCheckBox("LEFT-0 is on the right");
        toggleLeftRight.addActionListener(_ -> {
            manager.movePanel(
                "LEFT-0",
                new ToolPanel.Placement(
                    toggleLeftRight.isSelected() ? ToolPanel.Placement.Anchor.RIGHT : ToolPanel.Placement.Anchor.LEFT,
                    true),
                0);
        });

        var toolBar = new JToolBar();
        toolBar.add(dumpLayout);
        toolBar.add(saveLayout);
        toolBar.add(loadLayout);
        toolBar.add(restoreLayout);
        toolBar.addSeparator();
        toolBar.add(toggleLeftRight);

        JFrame frame = new JFrame("ToolManager");
        frame.add(manager.getComponent(), BorderLayout.CENTER);
        frame.add(toolBar, BorderLayout.NORTH);
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
