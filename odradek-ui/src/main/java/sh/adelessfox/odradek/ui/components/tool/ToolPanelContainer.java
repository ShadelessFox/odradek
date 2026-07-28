package sh.adelessfox.odradek.ui.components.tool;

import net.miginfocom.swing.MigLayout;
import sh.adelessfox.odradek.ui.Focusable;
import sh.adelessfox.odradek.ui.components.Orientation;
import sh.adelessfox.odradek.ui.components.Splitter;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A panel with buttons on either sides that reveal contents when clicked.
 * Clicking on an already selected button will unselect it and hide the contents.
 */
public final class ToolPanelContainer extends JComponent {
    private final Splitter groupSplitter = new Splitter(Orientation.VERTICAL); // splitter between primary and secondary groups
    private final Splitter outerSplitter = new Splitter(Orientation.HORIZONTAL); // splitter between the panel and contents
    private final JPanel buttonsPanel;

    private final List<ToolPanelButton> primaryButtons = new ArrayList<>();
    private final List<ToolPanelButton> secondaryButtons = new ArrayList<>();
    private final ToolPanelGroup primaryGroup = new ToolPanelGroup();
    private final ToolPanelGroup secondaryGroup = new ToolPanelGroup();
    private final Placement placement;

    private final Map<String, ToolPanel> panels = new HashMap<>();

    public enum Placement {
        LEFT,
        RIGHT
    }

    public ToolPanelContainer(Placement placement) {
        this.placement = placement;
        this.buttonsPanel = createButtonPane();

        setLayout(new BorderLayout());
        add(outerSplitter, BorderLayout.CENTER);
        add(buttonsPanel, switch (placement) {
            case LEFT -> BorderLayout.WEST;
            case RIGHT -> BorderLayout.EAST;
        });

        groupSplitter.setFirstComponent(primaryGroup.getComponent());
        groupSplitter.setSecondComponent(secondaryGroup.getComponent());

        switch (placement) {
            case LEFT -> outerSplitter.setFirstComponent(groupSplitter);
            case RIGHT -> outerSplitter.setSecondComponent(groupSplitter);
        }

        ButtonRepainter.install();
    }

    public JComponent getContent() {
        return switch (placement) {
            case LEFT -> outerSplitter.getSecondComponent();
            case RIGHT -> outerSplitter.getFirstComponent();
        };
    }

    public void setContent(JComponent content) {
        switch (placement) {
            case LEFT -> outerSplitter.setSecondComponent(content);
            case RIGHT -> outerSplitter.setFirstComponent(content);
        }
    }

    public void addPrimaryPanel(String id, String text, Icon icon, ToolPanel panel) {
        addPanel(id, text, icon, panel, true);
    }

    public void addSecondaryPanel(String id, String text, Icon icon, ToolPanel panel) {
        addPanel(id, text, icon, panel, false);
    }

    private void addPanel(String id, String text, Icon icon, ToolPanel panel, boolean primary) {
        if (panels.containsKey(id)) {
            throw new IllegalArgumentException("Panel with id '" + id + "' already exists");
        }

        var panelGroup = primary ? primaryGroup : secondaryGroup;
        panelGroup.addPanel(panel);

        var callback = (Runnable) () -> selectPanel(panelGroup, panel, !panelGroup.isSelected(panel));
        var button = new ToolPanelButton(panelGroup, panel, icon, callback);
        button.setToolTipText(text);

        var buttonGroup = primary ? primaryButtons : secondaryButtons;
        buttonGroup.add(button);

        // TODO: Find a better way to insert a separator when both groups are present
        buttonsPanel.removeAll();
        primaryButtons.forEach(buttonsPanel::add);
        int separatorIndex = buttonsPanel.getComponentCount();
        secondaryButtons.forEach(buttonsPanel::add);
        if (buttonsPanel.getComponentCount() != separatorIndex) {
            buttonsPanel.add(new JSeparator(), "al center,w 16", separatorIndex);
        }

        panels.put(id, panel);
    }

    public void showPanel(String id) {
        selectPanel(id, true);
    }

    public void hidePanel(String id) {
        selectPanel(id, false);
    }

    private void selectPanel(String id, boolean select) {
        var panel = panels.get(id);
        if (panel == null) {
            throw new IllegalArgumentException("No panel with id '" + id + "' found");
        }
        selectPanel(panel, select);
    }

    private void selectPanel(ToolPanel panel, boolean select) {
        if (primaryGroup.hasPanel(panel)) {
            selectPanel(primaryGroup, panel, select);
        } else {
            selectPanel(secondaryGroup, panel, select);
        }
    }

    private void selectPanel(ToolPanelGroup group, ToolPanel panel, boolean select) {
        if (!group.selectPanel(select ? panel : null)) {
            return;
        }

        boolean hasPrimary = primaryGroup.hasSelection();
        boolean hasSecondary = secondaryGroup.hasSelection();

        primaryGroup.getComponent().setVisible(hasPrimary);
        secondaryGroup.getComponent().setVisible(hasSecondary);
        groupSplitter.setVisible(hasPrimary || hasSecondary);

        if (select) {
            panel.setFocus();
        } else if (getContent() instanceof Focusable focusable) {
            focusable.setFocus();
        }
    }

    private static JPanel createButtonPane() {
        return new JPanel(new MigLayout("ins 0 4 0 4,gap 4,wrap"));
    }

    private static class ButtonRepainter implements PropertyChangeListener {
        private static ButtonRepainter instance;
        private final KeyboardFocusManager keyboardFocusManager;

        static void install() {
            synchronized (ButtonRepainter.class) {
                if (instance != null) {
                    return;
                }
                instance = new ButtonRepainter();
            }
        }

        ButtonRepainter() {
            keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            keyboardFocusManager.addPropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            switch (e.getPropertyName()) {
                case "permanentFocusOwner" -> {
                    Object oldValue = e.getOldValue();
                    Object newValue = e.getNewValue();
                    if (oldValue instanceof Component component) {
                        repaintSelectedPaneButtons(component);
                    }
                    if (newValue instanceof Component component) {
                        repaintSelectedPaneButtons(component);
                    }
                }
                case "activeWindow" -> {
                    Component permanentFocusOwner = keyboardFocusManager.getPermanentFocusOwner();
                    if (permanentFocusOwner != null) {
                        repaintSelectedPaneButtons(permanentFocusOwner);
                    }
                }
            }
        }

        private static void repaintSelectedPaneButtons(Component c) {
            if (c instanceof ToolPanelContainer panel) {
                repaintSelectedPaneButton(panel);
            }
            for (Component c2 = c; (c2 = SwingUtilities.getAncestorOfClass(ToolPanelContainer.class, c2)) != null; ) {
                repaintSelectedPaneButton((ToolPanelContainer) c2);
            }
        }

        private static void repaintSelectedPaneButton(ToolPanelContainer panel) {
            panel.buttonsPanel.repaint();
        }
    }
}
