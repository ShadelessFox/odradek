package sh.adelessfox.odradek.ui.components;

import java.awt.*;
import java.util.EnumSet;

abstract class StyledCellRenderer extends StyledComponent {
    @Override
    public void setText(StyledText text) {
        if (isSelected() && isFocused()) {
            var fragments = text.fragments().stream()
                .map(this::mapFocusedFragment)
                .toList();
            text = new StyledText(fragments);
        }
        super.setText(text);
    }

    private StyledFragment mapFocusedFragment(StyledFragment fragment) {
        var flags = EnumSet.noneOf(StyledFlag.class);
        flags.addAll(fragment.flags());
        flags.remove(StyledFlag.GRAYED);

        return fragment
            .withForeground(getForeground())
            .withFlags(flags);
    }

    protected abstract boolean isSelected();

    protected abstract boolean isFocused();

    //region Performance optimization; see javax.swing.tree.DefaultTreeCellRenderer
    @Override
    public void validate() {
    }

    @Override
    public void invalidate() {
    }

    @Override
    public void revalidate() {
    }

    @Override
    public void repaint(long tm, int x, int y, int width, int height) {
    }

    @Override
    public void repaint(Rectangle r) {
    }

    @Override
    public void repaint() {
    }
    //endregion
}
