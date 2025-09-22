package sh.adelessfox.odradek.ui.components.toolwindow;

import net.miginfocom.swing.MigLayout;
import sh.adelessfox.odradek.ui.Focusable;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * A panel with buttons on either sides that reveal contents when clicked.
 * Clicking on an already selected button will unselect it and hide the contents.
 */
public final class ToolWindowPanel extends JPanel {
    private final Splitter groupSplitter = new Splitter(true); // splitter between primary and secondary groups
    private final Splitter outerSplitter = new Splitter(false); // splitter between the panel and contents
    private final JPanel buttonsPanel;

    private final List<ToolWindowButton> primaryButtons = new ArrayList<>();
    private final List<ToolWindowButton> secondaryButtons = new ArrayList<>();
    private final ToolWindowGroup primaryGroup = new ToolWindowGroup();
    private final ToolWindowGroup secondaryGroup = new ToolWindowGroup();
    private final Placement placement;

    public enum Placement {
        LEFT,
        RIGHT
    }

    public ToolWindowPanel(Placement placement) {
        this.placement = placement;
        this.buttonsPanel = createButtonPane(placement);

        setLayout(new BorderLayout());
        add(outerSplitter, BorderLayout.CENTER);
        add(buttonsPanel, switch (placement) {
            case LEFT -> BorderLayout.WEST;
            case RIGHT -> BorderLayout.EAST;
        });

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

    public void addPrimaryPane(String text, Icon icon, ToolWindowPane pane) {
        addView(text, icon, pane, true);
    }

    public void addSecondaryPane(String text, Icon icon, ToolWindowPane pane) {
        addView(text, icon, pane, false);
    }

    private void addView(String text, Icon icon, ToolWindowPane pane, boolean primary) {
        var paneGroup = primary ? primaryGroup : secondaryGroup;
        var paneInfo = paneGroup.addPane(pane);

        var callback = (Runnable) () -> selectPane(paneGroup, paneInfo, !paneGroup.isSelected(paneInfo));
        var button = new ToolWindowButton(paneGroup, paneInfo, icon, callback);
        button.setToolTipText(text);

        var buttonGroup = primary ? primaryButtons : secondaryButtons;
        buttonGroup.add(button);

        // TODO: Find a better way to insert a separator when both groups are present
        buttonsPanel.removeAll();
        primaryButtons.forEach(buttonsPanel::add);
        int separatorIndex = buttonsPanel.getComponentCount();
        secondaryButtons.forEach(buttonsPanel::add);
        if (buttonsPanel.getComponentCount() != separatorIndex) {
            buttonsPanel.add(new JSeparator(), "growx", separatorIndex);
        }
    }

    public void showPane(ToolWindowPane pane) {
        selectPane(pane, true);
    }

    public void hidePane(ToolWindowPane pane) {
        selectPane(pane, false);
    }

    private void selectPane(ToolWindowPane pane, boolean select) {
        ToolWindowInfo info = primaryGroup.findPane(pane);
        if (info != null) {
            selectPane(primaryGroup, info, select);
            return;
        }
        info = secondaryGroup.findPane(pane);
        if (info != null) {
            selectPane(secondaryGroup, info, select);
            return;
        }
        throw new IllegalArgumentException("Pane does not belong to this panel");
    }

    private void selectPane(ToolWindowGroup group, ToolWindowInfo info, boolean select) {
        if (!group.selectPane(select ? info : null)) {
            return;
        }

        boolean hasPrimary = primaryGroup.hasSelection();
        boolean hasSecondary = secondaryGroup.hasSelection();

        groupSplitter.setFirstComponent(hasPrimary ? primaryGroup.getComponent() : null);
        groupSplitter.setSecondComponent(hasSecondary ? secondaryGroup.getComponent() : null);

        var content = hasPrimary || hasSecondary ? groupSplitter : null;
        switch (placement) {
            case LEFT -> outerSplitter.setFirstComponent(content);
            case RIGHT -> outerSplitter.setSecondComponent(content);
        }

        if (select) {
            info.pane().setFocus();
        } else if (getContent() instanceof Focusable focusable) {
            focusable.setFocus();
        }
    }

    private static JPanel createButtonPane(Placement placement) {
        var border = switch (placement) {
            case LEFT -> BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Separator.foreground"));
            case RIGHT -> BorderFactory.createMatteBorder(0, 1, 0, 0, UIManager.getColor("Separator.foreground"));
        };

        var panel = new JPanel();
        panel.setLayout(new MigLayout("ins 0,gap 0,wrap"));
        panel.setBorder(border);

        return panel;
    }

    private static final class Splitter extends JPanel {
        private final JSplitPane pane;
        private JComponent firstComponent;
        private JComponent secondComponent;

        Splitter(boolean vertical) {
            pane = new JSplitPane(vertical ? JSplitPane.VERTICAL_SPLIT : JSplitPane.HORIZONTAL_SPLIT);
            pane.setLeftComponent(null);
            pane.setRightComponent(null);

            setLayout(new BorderLayout());
        }

        JComponent getFirstComponent() {
            return firstComponent;
        }

        void setFirstComponent(JComponent component) {
            if (this.firstComponent != component) {
                expand(component, true);
                this.firstComponent = component;
            }
        }

        JComponent getSecondComponent() {
            return secondComponent;
        }

        void setSecondComponent(JComponent component) {
            if (this.secondComponent != component) {
                expand(component, false);
                this.secondComponent = component;
            }
        }

        private void expand(Component component, boolean left) {
            var opposite = left ? secondComponent : firstComponent;
            var dividerLocation = pane.getDividerLocation();

            removeAll();

            if (component == null) {
                if (opposite != null) {
                    add(opposite, BorderLayout.CENTER);
                }
            } else if (opposite == null) {
                add(component, BorderLayout.CENTER);
            } else {
                add(pane, BorderLayout.CENTER);
                pane.setLeftComponent(left ? component : opposite);
                pane.setRightComponent(left ? opposite : component);
                pane.setDividerLocation(dividerLocation);
            }

            revalidate();
        }
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
            if (c instanceof ToolWindowPanel panel) {
                repaintSelectedPaneButton(panel);
            }
            for (Component c2 = c; (c2 = SwingUtilities.getAncestorOfClass(ToolWindowPanel.class, c2)) != null; ) {
                repaintSelectedPaneButton((ToolWindowPanel) c2);
            }
        }

        private static void repaintSelectedPaneButton(ToolWindowPanel panel) {
            panel.buttonsPanel.repaint();
        }
    }
}
