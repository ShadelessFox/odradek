package sh.adelessfox.odradek.app.ui;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

// Mouse key remap logic is taken from
// https://github.com/JetBrains/intellij-community/blob/master/platform/platform-impl/src/com/intellij/ide/IdeEventQueue.kt
public final class ApplicationEventQueue extends EventQueue {
    private final boolean isXToolkit = Toolkit.getDefaultToolkit().getClass().getName().equals("sun.awt.X11.XToolkit");

    @Override
    protected void dispatchEvent(AWTEvent event) {
        super.dispatchEvent(mapEvent(event));
    }

    private AWTEvent mapEvent(AWTEvent event) {
        if (isXToolkit && event instanceof MouseEvent me && me.getButton() > 3) {
            return mapXWindowMouseEvent(me);
        } else {
            return event;
        }
    }

    @SuppressWarnings({"deprecation", "MagicConstant"})
    private AWTEvent mapXWindowMouseEvent(MouseEvent src) {
        if (src.getButton() < 6) {
            // buttons 4-5 come from touchpad, they must be converted to horizontal scrolling events
            return new MouseWheelEvent(
                src.getComponent(),
                MouseEvent.MOUSE_WHEEL,
                src.getWhen(),
                src.getModifiers() | InputEvent.SHIFT_DOWN_MASK,
                src.getX(),
                src.getY(),
                0,
                false,
                MouseWheelEvent.WHEEL_UNIT_SCROLL,
                src.getClickCount(),
                src.getButton() == 4 ? -1 : 1);
        } else {
            // Here we "shift" events with buttons `6` and `7` to similar events with buttons 4 and 5
            // See `java.awt.InputEvent#BUTTON_DOWN_MASK`, 1<<14 is the 4th physical button, 1<<15 is the 5th.
            return new MouseEvent(
                src.getComponent(),
                src.getID(),
                src.getWhen(),
                src.getModifiers() | 1 << 8 + src.getButton(),
                src.getX(),
                src.getY(),
                1,
                src.isPopupTrigger(),
                src.getButton() - 2);
        }
    }
}
