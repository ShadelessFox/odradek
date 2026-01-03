package sh.adelessfox.odradek.app.ui.component;

import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.rtti.runtime.TypedObject;
import sh.adelessfox.odradek.ui.Preview;
import sh.adelessfox.odradek.ui.components.tree.TreeItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

/**
 * Shows a tooltip-like popup with custom UI component for objects that have a {@link Preview}.
 */
public final class PreviewManager extends MouseAdapter {
    public interface PreviewObjectProvider {
        Optional<TypedObject> getObject(JTree tree, Object value);

        Optional<TypeInfo> getType(JTree tree, Object value);
    }

    private final JTree tree;
    private final Game game;
    private final PreviewObjectProvider provider;
    private final Timer enterTimer;

    // For caching last shown tooltip while hovering over the same row
    private int lastRowIndex;
    private int lastRowCount;
    private final Point lastLocation = new Point();

    private boolean showImmediately;
    private Popup popup;
    private JComponent component;

    private PreviewManager(JTree tree, Game game, PreviewObjectProvider provider) {
        this.tree = tree;
        this.game = game;
        this.provider = provider;

        lastRowIndex = -1;
        lastRowCount = -1;

        enterTimer = new Timer(750, this::enterTimerAction);
        enterTimer.setRepeats(false);

        tree.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0 &&
                e.getChanged() == tree &&
                e.getChangedParent() instanceof JViewport viewport
            ) {
                viewport.addChangeListener(_ -> {
                    if (showImmediately) {
                        var pointer = MouseInfo.getPointerInfo().getLocation();
                        var screen = tree.getLocationOnScreen();
                        showTip(new Point(pointer.x - screen.x, pointer.y - screen.y));
                    }
                });
            }
        });
    }

    public static void install(JTree tree, Game game, PreviewObjectProvider supplier) {
        var adapter = new PreviewManager(tree, game, supplier);
        tree.addMouseMotionListener(adapter);
        tree.addMouseListener(adapter);
    }

    private void enterTimerAction(ActionEvent e) {
        showImmediately = true;
        showTip(lastLocation);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (showImmediately) {
            showTip(e.getPoint());
        } else {
            lastLocation.x = e.getX();
            lastLocation.y = e.getY();
            enterTimer.start();
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        enterTimer.stop();
        showImmediately = false;
        hidePopup();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        enterTimer.stop();
        showImmediately = false;
        hidePopup();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (showImmediately) {
            showTip(e.getPoint());
        } else {
            lastLocation.x = e.getX();
            lastLocation.y = e.getY();
            enterTimer.restart();
        }
    }

    private void showTip(Point location) {
        // if ((e.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) == 0) {
        //     hidePopup();
        //     return;
        // }

        int rowIndex = tree.getRowForLocation(location.x, location.y);
        if (rowIndex < 0) {
            hidePopup();
            return;
        }

        int rowCount = tree.getRowCount();
        if (lastRowIndex == rowIndex && lastRowCount == rowCount) {
            showPopup(location, rowIndex, rowCount);
            return;
        }

        var path = tree.getPathForRow(rowIndex);
        if (path == null) {
            hidePopup();
            return;
        }

        var element = path.getLastPathComponent();
        if (element instanceof TreeItem<?> item) {
            element = item.getValue();
        }

        hidePopup();

        if (element == null) {
            return;
        }

        var converter = provider.getType(tree, element).stream()
            .flatMap(Converter::converters)
            .filter(c -> Preview.provider(c.outputType()).isPresent())
            .findFirst().orElse(null);

        if (converter == null) {
            return;
        }

        var preview = Preview.provider(converter.outputType()).orElse(null);
        if (preview == null) {
            return;
        }

        var object = provider.getObject(tree, element)
            .flatMap(o -> converter.convert(o, game))
            .orElse(null);

        if (object != null) {
            component = preview.create(object).createComponent();
            showPopup(location, rowIndex, rowCount);
        }
    }

    private void showPopup(Point location, int rowIndex, int rowCount) {
        lastRowIndex = rowIndex;
        lastRowCount = rowCount;

        if (popup != null) {
            popup.hide();
            popup = null;
        }

        if (component == null) {
            return;
        }

        var locationOnScreen = tree.getLocationOnScreen();
        int x = location.x + locationOnScreen.x + 20;
        int y = location.y + locationOnScreen.y + 20;

        popup = PopupFactory.getSharedInstance().getPopup(tree, component, x, y);
        popup.show();
    }

    private void hidePopup() {
        if (popup != null) {
            popup.hide();
            popup = null;
        }

        lastRowIndex = -1;
        lastRowCount = -1;
        component = null;
    }
}
