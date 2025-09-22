package sh.adelessfox.odradek.ui.components.view;

import com.formdev.flatlaf.extras.components.FlatButton;
import com.formdev.flatlaf.extras.components.FlatToggleButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * A panel with buttons on either sides that reveal contents when clicked.
 * Clicking on an already selected button will unselect it and hide the contents.
 */
public final class ViewPanel extends JPanel {
    private final Splitter groupSplitter = new Splitter(true); // splitter between primary and secondary groups
    private final Splitter outerSplitter = new Splitter(false); // splitter between the panel and contents
    private final JPanel buttonsPanel;

    private final ToggleButtonGroup primaryButtonGroup = new ToggleButtonGroup();
    private final ToggleButtonGroup secondaryButtonGroup = new ToggleButtonGroup();
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

        var buttonGroup = primary ? primaryButtonGroup : secondaryButtonGroup;
        var button = createButton(text, icon);

        button.addActionListener(_ -> selectView(viewGroup, viewInfo, buttonGroup.getSelection() != null));
        buttonGroup.add(button);

        // TODO: Find a better way to insert a separator when both groups are present
        buttonsPanel.removeAll();
        for (var e = primaryButtonGroup.getElements(); e.hasMoreElements(); ) {
            buttonsPanel.add(e.nextElement());
        }
        int separatorIndex = buttonsPanel.getComponentCount();
        for (var e = secondaryButtonGroup.getElements(); e.hasMoreElements(); ) {
            buttonsPanel.add(e.nextElement());
        }
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

    private AbstractButton createButton(String text, Icon icon) {
        var underlinePlacement = switch (placement) {
            case LEFT -> SwingConstants.RIGHT;
            case RIGHT -> SwingConstants.LEFT;
        };

        var button = new FlatToggleButton();
        button.setIcon(icon);
        button.setToolTipText(text);
        button.setButtonType(FlatButton.ButtonType.tab);
        button.setTabUnderlinePlacement(underlinePlacement);
        button.setMinimumSize(new Dimension(32, 32));

        return button;
    }

    /**
     * A button group that clears the selection when selecting an already selected button.
     */
    private static final class ToggleButtonGroup extends ButtonGroup {
        @Override
        public void setSelected(ButtonModel m, boolean b) {
            if (isSelected(m) && !b) {
                super.clearSelection();
            } else if (b) {
                super.setSelected(m, true);
            }
        }
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

        void setFirstComponent(JComponent component) {
            if (this.firstComponent != component) {
                expand(component, true);
                this.firstComponent = component;
            }
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
}
