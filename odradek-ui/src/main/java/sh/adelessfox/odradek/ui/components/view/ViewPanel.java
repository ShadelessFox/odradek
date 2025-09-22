package sh.adelessfox.odradek.ui.components.view;

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
public final class ViewPanel extends JPanel {
    private final Splitter groupSplitter = new Splitter(true); // splitter between primary and secondary groups
    private final Splitter outerSplitter = new Splitter(false); // splitter between the panel and contents
    private final JPanel buttonsPanel;

    private final List<ViewButton> primaryButtons = new ArrayList<>();
    private final List<ViewButton> secondaryButtons = new ArrayList<>();
    private final ViewGroup primaryGroup = new ViewGroup();
    private final ViewGroup secondaryGroup = new ViewGroup();
    private final Placement placement;

    public enum Placement {
        LEFT,
        RIGHT
    }

    public ViewPanel(Placement placement) {
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

    public void addPrimaryView(String text, Icon icon, View view) {
        addView(text, icon, view, true);
    }

    public void addSecondaryView(String text, Icon icon, View view) {
        addView(text, icon, view, false);
    }

    public void selectView(View view, boolean select) {
        // TODO don't forget about button selection
    }

    private void addView(String text, Icon icon, View view, boolean primary) {
        var viewGroup = primary ? primaryGroup : secondaryGroup;
        var viewInfo = viewGroup.addView(view);

        var callback = (Runnable) () -> selectView(viewGroup, viewInfo, !viewGroup.isSelected(viewInfo));
        var button = new ViewButton(viewGroup, viewInfo, icon, callback);
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

    private void selectView(ViewGroup group, ViewInfo info, boolean select) {
        if (!group.selectView(select ? info : null)) {
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
            info.view().setFocus();
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
                        repaintSelectedViewButtons(component);
                    }
                    if (newValue instanceof Component component) {
                        repaintSelectedViewButtons(component);
                    }
                }
                case "activeWindow" -> {
                    Component permanentFocusOwner = keyboardFocusManager.getPermanentFocusOwner();
                    if (permanentFocusOwner != null) {
                        repaintSelectedViewButtons(permanentFocusOwner);
                    }
                }
            }
        }

        private static void repaintSelectedViewButtons(Component c) {
            if (c instanceof ViewPanel panel) {
                repaintSelectedViewButton(panel);
            }
            for (Component c2 = c; (c2 = SwingUtilities.getAncestorOfClass(ViewPanel.class, c2)) != null; ) {
                repaintSelectedViewButton((ViewPanel) c2);
            }
        }

        private static void repaintSelectedViewButton(ViewPanel panel) {
            panel.buttonsPanel.repaint();
        }
    }
}
