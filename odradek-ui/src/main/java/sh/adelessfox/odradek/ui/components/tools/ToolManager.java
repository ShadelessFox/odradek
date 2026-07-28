package sh.adelessfox.odradek.ui.components.tools;

import javax.swing.*;

public interface ToolManager {
    /**
     * Returns the main container for tool panels.
     *
     * @return main component
     */
    JComponent getComponent();

    /**
     * Sets the central component.
     *
     * @param component central component
     */
    void setCenter(JComponent component);

    /**
     * Registers a new tool panel using the given provider.
     *
     * @param provider  provider of the tool panel
     * @param placement placement of the tool panel
     */
    void addPanel(ToolPanel.Provider provider, ToolPanel.Placement placement);

    /**
     * Opens the tool panel with the given ID.
     *
     * @param id    ID of the tool panel
     * @param focus whether to focus the panel component after opening
     */
    void openPanel(String id, boolean focus);

    /**
     * Closes the tool panel with the given ID.
     *
     * @param id ID of the tool panel
     */
    void closePanel(String id);

    /**
     * Moves the tool panel with the given ID to a new anchor position.
     *
     * @param id        ID of the tool panel
     * @param placement new placement of the tool panel
     * @param index     index of the panel in the new anchor position
     */
    void movePanel(String id, ToolPanel.Placement placement, int index);

    ToolState getState();

    void setState(ToolState state);
}
