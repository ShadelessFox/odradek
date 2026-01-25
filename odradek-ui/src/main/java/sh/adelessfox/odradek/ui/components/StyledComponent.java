package sh.adelessfox.odradek.ui.components;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.ColorFunctions;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StyledComponent extends JComponent {
    private static final boolean DEBUG_OVERLAY = false;

    private final List<StyledFragment> fragments = new ArrayList<>(3);

    private Icon leadingIcon;
    private Icon trailingIcon;
    private Insets padding;
    private int iconTextGap;

    public StyledComponent() {
        iconTextGap = UIScale.scale(4);
        padding = new Insets(1, 2, 1, 2);
        setOpaque(true);
        updateUI();
    }

    public void append(StyledFragment fragment) {
        synchronized (fragments) {
            fragments.add(fragment);
        }
    }

    public void clear() {
        synchronized (fragments) {
            fragments.clear();
            leadingIcon = null;
            trailingIcon = null;
        }
    }

    public Icon getLeadingIcon() {
        return leadingIcon;
    }

    public void setLeadingIcon(Icon leadingIcon) {
        if (Objects.equals(this.leadingIcon, leadingIcon)) {
            return;
        }
        this.leadingIcon = leadingIcon;
    }

    public Icon getTrailingIcon() {
        return trailingIcon;
    }

    public void setTrailingIcon(Icon trailingIcon) {
        if (Objects.equals(this.trailingIcon, trailingIcon)) {
            return;
        }
        this.trailingIcon = trailingIcon;
    }

    public int getIconTextGap() {
        return iconTextGap;
    }

    public void setIconTextGap(int iconTextGap) {
        if (iconTextGap < 0) {
            throw new IllegalArgumentException("iconTextGap < 0: " + iconTextGap);
        }
        if (this.iconTextGap == iconTextGap) {
            return;
        }
        this.iconTextGap = iconTextGap;
    }

    public Insets getPadding() {
        return padding;
    }

    public void setPadding(Insets padding) {
        if (this.padding.equals(padding)) {
            return;
        }
        this.padding = padding;
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        return new Dimension((int) Math.ceil(computePreferredWidth()), computePreferredHeight());
    }

    @Override
    public Dimension getMinimumSize() {
        if (isMinimumSizeSet()) {
            return super.getMinimumSize();
        }
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        doPaint((Graphics2D) g);
    }

    @Override
    public String toString() {
        synchronized (fragments) {
            return fragments.stream()
                .map(Objects::toString)
                .reduce("", String::concat);
        }
    }

    private void doPaint(Graphics2D g) {
        int offset = 0;

        if (leadingIcon != null) {
            doPaintIconBackground(g, leadingIcon, offset, padding.left);
            offset += padding.left;
            doPaintIcon(g, leadingIcon, offset);
            offset += leadingIcon.getIconWidth() + iconTextGap;
        }

        doPaintTextBackground(g, offset);
        offset += doPaintTextFragments(g, offset);

        if (trailingIcon != null) {
            offset += iconTextGap;
            doPaintIconBackground(g, trailingIcon, offset, padding.right);
            doPaintIcon(g, trailingIcon, offset);
        }
    }

    private void doPaintIcon(Graphics2D g, Icon icon, int offset) {
        Rectangle area = computePaintArea();
        int y = area.y + (area.height - icon.getIconHeight()) / 2;

        if (DEBUG_OVERLAY) {
            g.setColor(Color.LIGHT_GRAY);
            g.drawRect(offset, y, icon.getIconWidth() - 1, icon.getIconHeight() - 1);
        }

        icon.paintIcon(this, g, offset, y);
    }

    private void doPaintIconBackground(Graphics2D g, Icon icon, int offset, int padding) {
        if (isOpaque()) {
            g.setColor(getBackground());
            g.fillRect(offset, 0, padding + icon.getIconWidth() + iconTextGap, getHeight());
        }
    }

    private int doPaintTextFragments(Graphics2D g, int startOffset) {
        FlatUIUtils.setRenderingHints(g);
        setTextRenderingHints(g);

        float offset = startOffset;

        if (leadingIcon == null) {
            offset += padding.left;
        }

        Rectangle area = computePaintArea();
        Font font = getBaseFont();

        synchronized (fragments) {
            for (StyledFragment fragment : fragments) {
                FontMetrics metrics = getFontMetrics(font);

                var fragmentBaseline = area.y + area.height - metrics.getDescent();
                var fragmentWidth = computeFragmentWidth(fragment, font);
                var fragmentColor = computeFragmentForeground(fragment);

                if (DEBUG_OVERLAY) {
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawRect((int) offset, area.y, (int) fragmentWidth - 1, area.height - 1);
                }

                g.setFont(font);
                g.setColor(fragmentColor);
                g.drawString(fragment.text(), offset, fragmentBaseline);

                offset = offset + fragmentWidth;
            }
        }

        return (int) offset - startOffset;
    }

    private static void setTextRenderingHints(Graphics2D g2) {
        Object aaHint = UIManager.get(RenderingHints.KEY_TEXT_ANTIALIASING);
        if (aaHint != null) {
            Object oldAA = g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
            if (aaHint != oldAA) {
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, aaHint);
            }
        }

        Object contrastHint = UIManager.get(RenderingHints.KEY_TEXT_LCD_CONTRAST);
        if (contrastHint != null) {
            Object oldContrast = g2.getRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST);
            if (contrastHint != oldContrast) {
                g2.setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST, contrastHint);
            }
        }
    }

    private void doPaintTextBackground(Graphics2D g, int offset) {
        if (isOpaque()) {
            g.setColor(getBackground());
            g.fillRect(offset, 0, getWidth() - offset, getHeight());
        }
    }

    private Rectangle computePaintArea() {
        Rectangle area = new Rectangle(getPreferredSize());
        area.x += (getWidth() - area.width) / 2;
        area.y += (getHeight() - area.height) / 2;

        removeFrom(area, getInsets());
        removeFrom(area, padding);

        return area;
    }

    private static void removeFrom(Rectangle rect, Insets insets) {
        if (insets != null) {
            rect.x += insets.left;
            rect.y += insets.top;
            rect.width -= insets.left + insets.right;
            rect.height -= insets.top + insets.bottom;
        }
    }

    private Color computeFragmentForeground(StyledFragment fragment) {
        var foreground = fragment.foreground().orElse(null);
        if (fragment.flags().contains(StyledFlag.GRAYED)) {
            if (foreground != null) {
                return ColorFunctions.mix(foreground, getBackground(), 0.5f);
            } else {
                return UIManager.getColor("Label.disabledForeground");
            }
        } else {
            if (foreground != null) {
                return foreground;
            } else {
                return getForeground();
            }
        }
    }

    private float computeFragmentWidth(StyledFragment fragment, Font font) {
        var metrics = getFontMetrics(font);
        var bounds = font.getStringBounds(fragment.text(), metrics.getFontRenderContext());
        return (float) bounds.getWidth();
    }

    private float computePreferredWidth() {
        Insets insets = getInsets();
        Font font = getBaseFont();

        float width = 0;

        width += padding.left + insets.left;
        width += padding.right + insets.right;

        synchronized (fragments) {
            for (StyledFragment fragment : fragments) {
                width += computeFragmentWidth(fragment, font);
            }
        }

        if (leadingIcon != null) {
            width += leadingIcon.getIconWidth() + iconTextGap;
        }

        if (trailingIcon != null) {
            width += trailingIcon.getIconWidth() + iconTextGap;
        }

        return width;
    }

    private int computePreferredHeight() {
        Font font = getBaseFont();
        FontMetrics metrics = getFontMetrics(font);
        Insets insets = getInsets();

        int height = Math.max(UIScale.scale(16), metrics.getHeight());

        if (leadingIcon != null) {
            height = Math.max(height, leadingIcon.getIconHeight());
        }

        if (trailingIcon != null) {
            height = Math.max(height, trailingIcon.getIconHeight());
        }

        height += padding.top + insets.top;
        height += padding.bottom + insets.bottom;

        return height;
    }

    private Font getBaseFont() {
        Font font = getFont();

        if (font == null) {
            font = UIManager.getFont("Label.font");
        }

        return font;
    }
}
