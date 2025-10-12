package sh.adelessfox.odradek.app.editors;

import com.formdev.flatlaf.extras.components.FlatTabbedPane;
import sh.adelessfox.odradek.app.ObjectStructure;
import sh.adelessfox.odradek.app.menu.ActionIds;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.game.hfw.rtti.data.StreamingLink;
import sh.adelessfox.odradek.rtti.*;
import sh.adelessfox.odradek.rtti.data.Value;
import sh.adelessfox.odradek.rtti.runtime.TypedObject;
import sh.adelessfox.odradek.ui.Viewer;
import sh.adelessfox.odradek.ui.actions.Actions;
import sh.adelessfox.odradek.ui.components.tree.StructuredTree;
import sh.adelessfox.odradek.ui.components.tree.TreeItem;
import sh.adelessfox.odradek.ui.components.tree.TreeLabelProvider;
import sh.adelessfox.odradek.ui.editors.Editor;
import sh.adelessfox.odradek.ui.editors.EditorInput;
import sh.adelessfox.odradek.ui.editors.EditorManager;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

final class ObjectEditor implements Editor {
    private final ObjectEditorInput input;
    private FlatTabbedPane pane;

    public ObjectEditor(ObjectEditorInput input) {
        this.input = input;
    }

    @Override
    public JComponent createComponent() {
        if (pane == null) {
            pane = createRoot(input.game(), input.object());
        }
        return pane;
    }

    @Override
    public EditorInput getInput() {
        return input;
    }

    @Override
    public boolean isFocused() {
        return pane.getSelectedComponent().isFocusOwner();
    }

    @Override
    public void setFocus() {
        pane.getSelectedComponent().requestFocusInWindow();
    }

    private static FlatTabbedPane createRoot(Game game, TypedObject object) {
        FlatTabbedPane pane = new FlatTabbedPane();
        pane.setTabPlacement(SwingConstants.BOTTOM);
        pane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        Converter.converters(object).forEach(converter -> {
            @SuppressWarnings("unchecked")
            var clazz = (Class<Object>) converter.resultType();
            Viewer.viewers(clazz).forEach(viewer -> {
                var result = converter.convert(object, game);
                if (result.isEmpty()) {
                    return;
                }
                pane.add(viewer.displayName(), viewer.createPreview(result.get()));
            });
        });

        pane.add("Object", new JScrollPane(createObjectTree(game, object)));
        pane.setSelectedIndex(0);

        return pane;
    }

    private static StructuredTree<?> createObjectTree(Game game, TypedObject object) {
        var tree = new StructuredTree<>(new ObjectStructure.Compound(game, object.getType(), object));
        tree.setTransferHandler(new ObjectEditorTransferHandler());
        tree.setLabelProvider(new TreeLabelProvider<>() {
            @Override
            public Optional<String> getText(ObjectStructure element) {
                return Optional.of(element.toString());
            }

            @Override
            public Optional<Icon> getIcon(ObjectStructure element) {
                return Optional.of(Fugue.getIcon("blue-document"));
            }

            @Override
            public Optional<String> getToolTip(ObjectStructure element) {
                return Optional.of(getElementToolTip(element));
            }
        });
        tree.addActionListener(event -> {
            var component = event.getLastPathComponent();
            if (component instanceof TreeItem<?> wrapper) {
                component = wrapper.getValue();
            }
            if (component instanceof ObjectStructure structure && structure.value() instanceof StreamingLink<?> link) {
                var input = new ObjectEditorInput(game, link.get(), link.groupId(), link.objectIndex());
                EditorManager.sharedInstance().openEditor(input);
            }
        });
        Actions.installContextMenu(tree, ActionIds.OBJECT_MENU_ID, tree);
        return tree;
    }

    private static String getElementToolTip(ObjectStructure element) {
        TypeInfo type = element.type();
        StringBuilder buf = new StringBuilder();

        buf.append("<html><table>");
        switch (type) {
            case AtomTypeInfo i -> {
                appendSection(buf, "Atom");
                appendRow(buf, "Type", getTypeHierarchy(type, false));
                appendRow(buf, "Base", getTypeHierarchy(i.base().orElse(i), false));
            }
            case EnumTypeInfo i -> {
                var value = (Value<?>) element.value();
                appendSection(buf, "Enum");
                appendRow(buf, "Type", getTypeHierarchy(type, false));
                appendRow(buf, "Size", i.size() == 1 ? "1 byte" : i.size() + " bytes");
                appendRow(buf, "Value", toText(value.value()));
            }
            case ClassTypeInfo i -> {
                appendSection(buf, "Class");
                appendRow(buf, "Type", getTypeHierarchy(type, true));
                appendRow(buf, "Version", toText(i.version()));
                appendRow(buf, "Flags", toText(i.flags()));
            }
            case ContainerTypeInfo i -> {
                appendSection(buf, "Container");
                appendRow(buf, "Type", getTypeHierarchy(type, false));
                appendRow(buf, "Name", i.containerType());
                appendRow(buf, "Item", getTypeHierarchy(i.itemType(), false));
            }
            case PointerTypeInfo i -> {
                appendSection(buf, "Pointer");
                appendRow(buf, "Type", getTypeHierarchy(type, false));
                appendRow(buf, "Name", i.pointerType());
                appendRow(buf, "Item", getTypeHierarchy(i.itemType(), false));
            }
        }
        if (element instanceof ObjectStructure.Attr(_, _, var attr, _)) {
            appendSection(buf, "Attribute");
            appendRow(buf, "Flags", toText(attr.flags()));
            appendRow(buf, "Min value", attr.min().orElse("NOT SET"));
            appendRow(buf, "Max value", attr.max().orElse("NOT SET"));
        }
        buf.append("</table></html>");

        return buf.toString();
    }

    private static String getTypeHierarchy(TypeInfo info, boolean classHierarchy) {
        StringBuilder buf = new StringBuilder(100);
        getTypeHierarchy0(buf, info, classHierarchy ? 0 : -1);
        return buf.toString();
    }

    private static void getTypeHierarchy0(StringBuilder buf, TypeInfo info, int level) {
        if (level > 0) {
            buf.append("<br>&nbsp;");
            buf.append("&nbsp;&nbsp;&nbsp;&nbsp;".repeat(level - 1));
            buf.append("╰ ");
        }

        buf.append(info.name());
        buf.append(' ').append(toDimmedText("(" + info.type().getName() + ")"));

        if (level >= 0 && info instanceof ClassTypeInfo clazz) {
            for (ClassBaseInfo base : clazz.bases()) {
                getTypeHierarchy0(buf, base.type(), level + 1);
            }
        }
    }

    private static void appendSection(StringBuilder buf, String name) {
        buf.append("<tr><td><b>")
            .append(name)
            .append("</b></td></tr>");
    }

    private static void appendRow(StringBuilder buf, String key, Object value) {
        buf.append("<tr><td valign=\"top\">")
            .append(key)
            .append(":</td><td>")
            .append(value)
            .append("</td></tr>");
    }

    private static String toText(int value) {
        return value + toDimmedText(" (%#x)".formatted(value));
    }

    private static String toDimmedText(String text) {
        Color color = UIManager.getColor("Label.disabledForeground");
        if (color == null) {
            color = UIManager.getColor("Label.disabledText");
        }
        if (color == null) {
            color = Color.GRAY;
        }
        return String.format("<span color=\"#%06x\">%s</span>", color.getRGB() & 0xffffff, text);
    }
}
