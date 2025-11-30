package sh.adelessfox.odradek.viewer.model.viewport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.math.Vector2f;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles input events for a viewport, including mouse and keyboard events.
 */
public final class ViewportInput extends MouseAdapter implements KeyListener, FocusListener {
    private static final Logger log = LoggerFactory.getLogger(ViewportInput.class);

    private static final Cursor EMPTY_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor(
        new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB),
        new Point(0, 0),
        "empty cursor"
    );

    private final Component viewport;
    private final Robot robot;

    private final Set<Integer> mouseState = new HashSet<>();
    private final Set<Integer> keysDown = new HashSet<>();
    private final Set<Integer> keysPressed = new HashSet<>();

    private final Point mouseRecent = new Point();
    private final Point mouseDelta = new Point();
    private float mouseWheelDelta;

    public ViewportInput(Component viewport) {
        this.viewport = viewport;
        this.robot = tryCreateRobot();

        viewport.addMouseListener(this);
        viewport.addMouseMotionListener(this);
        viewport.addMouseWheelListener(this);
        viewport.addKeyListener(this);
        viewport.addFocusListener(this);
    }

    public boolean isKeyDown(int keyCode) {
        return keysDown.contains(keyCode);
    }

    public boolean isKeyPressed(int keyCode) {
        return keysPressed.contains(keyCode);
    }

    public boolean isMouseDown(int button) {
        return mouseState.contains(button);
    }

    public Vector2f mousePositionDelta() {
        return new Vector2f(mouseDelta.x, mouseDelta.y);
    }

    public float mouseWheelDelta() {
        return mouseWheelDelta;
    }

    public void clear() {
        mouseDelta.x = 0;
        mouseDelta.y = 0;
        mouseWheelDelta = 0;
        keysPressed.clear();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseState.add(e.getButton());

        if (mouseState.size() > 1) {
            // Another button was already pressed; don't override the state
            return;
        }

        mouseRecent.x = e.getX();
        mouseRecent.y = e.getY();
        mouseDelta.x = 0;
        mouseDelta.y = 0;
        SwingUtilities.convertPointToScreen(mouseRecent, viewport);

        if (robot != null) {
            viewport.setCursor(EMPTY_CURSOR);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseState.remove(e.getButton());

        if (!mouseState.isEmpty()) {
            // If any other button is still pressed, don't reset the state
            return;
        }

        if (robot != null) {
            viewport.setCursor(null);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (robot != null && mouseState.isEmpty()) {
            viewport.setCursor(null);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        var mouse = e.getLocationOnScreen();
        var bounds = new Rectangle(viewport.getLocationOnScreen(), viewport.getSize());

        // Shrink the bounds in case the viewport covers the entire screen
        bounds.x += 1;
        bounds.y += 1;
        bounds.width -= 2;
        bounds.height -= 2;

        if (robot != null && !bounds.contains(mouse)) {
            mouse.x = wrapAround(mouse.x, bounds.x, bounds.x + bounds.width);
            mouse.y = wrapAround(mouse.y, bounds.y, bounds.y + bounds.height);

            robot.mouseMove(mouse.x, mouse.y);
        } else {
            mouseDelta.x += mouse.x - mouseRecent.x;
            mouseDelta.y += mouse.y - mouseRecent.y;
        }

        mouseRecent.x = mouse.x;
        mouseRecent.y = mouse.y;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        mouseWheelDelta -= (float) e.getPreciseWheelRotation();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // do nothing
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keysDown.add(e.getKeyCode());
        keysPressed.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keysDown.remove(e.getKeyCode());
    }

    @Override
    public void focusGained(FocusEvent e) {
        // do nothing
    }

    @Override
    public void focusLost(FocusEvent e) {
        keysDown.clear();
        mouseState.clear();
    }

    private static Robot tryCreateRobot() {
        try {
            return new Robot();
        } catch (AWTException e) {
            log.error("Couldn't create robot! Mouse movement won't be constrained within the viewport.", e);
            return null;
        }
    }

    private static int wrapAround(int value, int min, int max) {
        if (value < min) {
            return max - (min - value) % (max - min);
        } else {
            return min + (value - min) % (max - min);
        }
    }
}
