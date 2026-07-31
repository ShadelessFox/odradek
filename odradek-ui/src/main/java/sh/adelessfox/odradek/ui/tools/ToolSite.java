package sh.adelessfox.odradek.ui.tools;

import javax.swing.*;

public interface ToolSite {
    /**
     * Sets the leading component of the associated tool panel's header.
     *
     * @param component the component to set as the leading component,
     *                  or {@code null} to remove the leading component
     */
    void setLeadingComponent(JComponent component);

    /**
     * Sets the trailing component of the associated tool panel's header.
     *
     * @param component the component to set as the trailing component,
     *                  or {@code null} to remove the trailing component
     */
    void setTrailingComponent(JComponent component);
}
