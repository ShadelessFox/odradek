package sh.adelessfox.odradek.ui;

import javax.swing.*;

public interface Panel extends Focusable {
    enum Placement {
        LEFT,
        RIGHT,
        BOTTOM,
        TOP
    }

    @interface Registration {
        String name();

        String icon();

        Placement placement() default Placement.LEFT;

        boolean primary() default true;

        int order() default Integer.MAX_VALUE;
    }

    JComponent createComponent();
}
