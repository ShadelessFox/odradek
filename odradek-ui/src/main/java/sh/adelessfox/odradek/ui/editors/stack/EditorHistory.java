package sh.adelessfox.odradek.ui.editors.stack;

import sh.adelessfox.odradek.ui.editors.Editor;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class EditorHistory extends JComponent {
    private final List<EditorComponent> entries = new ArrayList<>();
    private final JPanel cards = new JPanel(new CardLayout());
    private final Action back = new NavigationAction("Back", "arrow-180", "alt LEFT", -1);
    private final Action forward = new NavigationAction("Forward", "arrow", "alt RIGHT", 1);
    private final JToolBar navigationToolBar = new JToolBar();

    private int index;
    private boolean active;

    EditorHistory(EditorComponent initial) {
        setLayout(new BorderLayout());
        entries.add(initial);
        cards.add(initial, "0");

        navigationToolBar.setOpaque(false);
        navigationToolBar.add(Box.createHorizontalGlue());
        navigationToolBar.add(back).setFocusable(false);
        navigationToolBar.add(forward).setFocusable(false);

        add(cards, BorderLayout.CENTER);
        updateNavigation();
    }

    EditorComponent current() {
        return entries.get(index);
    }

    JToolBar getNavigationToolBar() {
        return navigationToolBar;
    }

    EditorStack getEditorStack() {
        return (EditorStack) getParent();
    }

    Optional<EditorComponent> find(Editor editor) {
        return entries.stream()
            .filter(entry -> entry.editor == editor)
            .findFirst();
    }

    void activate() {
        if (!active) {
            current().initialize();
            active = true;
            current().editor.activate();
        }
    }

    void deactivate() {
        if (active) {
            active = false;
            current().editor.deactivate();
        }
    }

    void navigate(EditorComponent entry) {
        if (active) {
            entry.initialize();
        }
        while (entries.size() > index + 1) {
            var discarded = entries.removeLast();
            cards.remove(discarded);
            discarded.editor.dispose();
        }
        entries.add(entry);
        cards.add(entry, Integer.toString(entries.size() - 1));
        show(entries.size() - 1);
    }

    void replace(EditorComponent oldEntry, EditorComponent newEntry) {
        int entryIndex = entries.indexOf(oldEntry);
        boolean selected = entryIndex == index;
        boolean wasActive = active;
        boolean focused = selected && active && oldEntry.editor.isFocused();

        if (selected && active) {
            newEntry.initialize();
            deactivate();
        }

        entries.set(entryIndex, newEntry);
        cards.remove(oldEntry);
        cards.add(newEntry, Integer.toString(entryIndex));
        oldEntry.editor.dispose();

        showCurrentCard();
        if (selected && wasActive) {
            activate();
            if (focused) {
                newEntry.editor.setFocus();
            }
        }
    }

    void dispose() {
        deactivate();
        for (var entry : entries) {
            entry.editor.dispose();
        }
        entries.clear();
        cards.removeAll();
    }

    private void show(int newIndex) {
        if (newIndex < 0 || newIndex >= entries.size() || newIndex == index) {
            return;
        }
        boolean wasActive = active;
        if (wasActive) {
            entries.get(newIndex).initialize();
        }
        deactivate();
        index = newIndex;
        showCurrentCard();
        if (wasActive) {
            activate();
            current().editor.setFocus();
        }
    }

    private void showCurrentCard() {
        ((CardLayout) cards.getLayout()).show(cards, Integer.toString(index));
        updateNavigation();
        var stack = getEditorStack();
        if (stack != null) {
            int tabIndex = stack.indexOfComponent(this);
            var input = current().editor.getInput();
            stack.setTitleAt(tabIndex, input.getName());
            stack.setToolTipTextAt(tabIndex, input.getDescription());
        }
        revalidate();
        repaint();
    }

    private void updateNavigation() {
        back.setEnabled(index > 0);
        forward.setEnabled(index + 1 < entries.size());
    }

    private final class NavigationAction extends AbstractAction {
        private final int direction;

        NavigationAction(String name, String icon, String keystroke, int direction) {
            super(name, Fugue.getIcon(icon));
            this.direction = direction;
            putValue(SHORT_DESCRIPTION, name + (direction < 0 ? " (Alt+Left)" : " (Alt+Right)"));
            getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(keystroke), name);
            getActionMap().put(name, this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            show(index + direction);
        }
    }
}
