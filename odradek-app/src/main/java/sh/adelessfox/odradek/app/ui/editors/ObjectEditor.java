package sh.adelessfox.odradek.app.ui.editors;

import com.formdev.flatlaf.extras.components.FlatTabbedPane;
import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.component.PreviewManager;
import sh.adelessfox.odradek.app.ui.menu.object.ObjectMenu;
import sh.adelessfox.odradek.game.*;
import sh.adelessfox.odradek.game.hfw.rtti.data.StreamingLink;
import sh.adelessfox.odradek.rtti.*;
import sh.adelessfox.odradek.rtti.data.Ref;
import sh.adelessfox.odradek.rtti.data.Value;
import sh.adelessfox.odradek.rtti.runtime.TypedObject;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.Viewer;
import sh.adelessfox.odradek.ui.actions.Actions;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;
import sh.adelessfox.odradek.ui.components.tree.StructuredTree;
import sh.adelessfox.odradek.ui.components.tree.StyledTreeLabelProvider;
import sh.adelessfox.odradek.ui.data.DataContext;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.editors.Editor;
import sh.adelessfox.odradek.ui.editors.EditorInput;
import sh.adelessfox.odradek.ui.editors.EditorSite;
import sh.adelessfox.odradek.ui.util.Fugue;
import sh.adelessfox.odradek.ui.util.Icons;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

final class ObjectEditor implements Editor, ObjectHolder, ObjectIdHolder, DataContext {
    private final ObjectEditorInput input;
    private final EditorSite site;
    private FlatTabbedPane pane;

    public ObjectEditor(ObjectEditorInput input, EditorSite site) {
        this.input = input;
        this.site = site;
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

    @Override
    public TypedObject readObject(Game game) {
        if (input.game() != game) {
            throw new IllegalStateException("Unexpected game type");
        }
        return input.object();
    }

    @Override
    public ClassTypeInfo objectType() {
        return input.object().getType();
    }

    @Override
    public String objectName() {
        return "%s_%s_%s".formatted(objectType().name(), input.groupId(), input.objectIndex());
    }

    @Override
    public ObjectId objectId() {
        return new ObjectId(input.groupId(), input.objectIndex());
    }

    @Override
    public Optional<?> get(String key) {
        if (DataKeys.GAME.is(key)) {
            return Optional.of(input.game());
        }
        if (DataKeys.SELECTION_LIST.is(key)) {
            return Optional.of(List.of(this));
        }
        return Optional.empty();
    }

    private FlatTabbedPane createRoot(Game game, TypedObject object) {
        FlatTabbedPane pane = new FlatTabbedPane();
        pane.setTabPlacement(SwingConstants.BOTTOM);
        pane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        pane.setLeadingComponent(Actions.createToolBar(ObjectEditorActionIds.TOOLBAR_ID, this));

        Converter.converters(object.getType()).forEach(converter -> {
            Viewer.viewers(converter.outputType()).forEach(viewer -> {
                var result = converter.convert(object, game);
                if (result.isEmpty()) {
                    return;
                }
                pane.insertTab(
                    viewer.name(),
                    viewer.icon().flatMap(Icons::getIconFromUri).orElse(null),
                    viewer.createComponent(result.get()),
                    null,
                    pane.getTabCount()
                );
            });
        });

        pane.insertTab(
            "Object",
            Fugue.getIcon("blue-document"),
            new JScrollPane(createObjectTree(game, object)),
            null,
            pane.getTabCount()
        );
        pane.setSelectedIndex(0);

        return pane;
    }

    private StructuredTree<?> createObjectTree(Game game, TypedObject object) {
        var tree = new StructuredTree<>(new ObjectStructure.Compound(game, object.getType(), object));
        tree.setTransferHandler(new ObjectEditorTransferHandler());
        tree.setLabelProvider(new ObjectEditorLabelProvider());
        tree.addActionListener(event -> {
            var component = event.getLastPathComponent();
            if (component instanceof ObjectStructure structure && structure.value() instanceof StreamingLink<?> link) {
                var input = new ObjectEditorInput(game, link.get(), link.groupId(), link.objectIndex());
                site.getManager().openEditor(input);
            }
        });
        Actions.installContextMenu(tree, ObjectMenu.ID, tree.or(key -> {
            if (DataKeys.GAME.is(key)) {
                return Optional.of(input.game());
            }
            return Optional.empty();
        }));
        PreviewManager.install(tree, game, new ObjectPreviewObjectProvider());
        return tree;
    }

    // region Text
    private static Optional<Transferable> getElementTransferable(ObjectStructure s) {
        return valueTextBuilder(s, false)
            .map(b -> b.apply(StyledText.builder()))
            .map(b -> b.build().toString())
            .map(StringSelection::new);
    }

    private static StyledText getElementText(ObjectStructure s) {
        var builder = StyledText.builder();
        keyTextBuilder(s).ifPresent(b -> b.apply(builder));
        builder.add("{" + s.type() + "} ", StyledFragment.GRAYED);
        valueTextBuilder(s, true).ifPresent(b -> b.apply(builder));
        return builder.build();
    }

    private static Optional<Function<StyledText.Builder, StyledText.Builder>> keyTextBuilder(ObjectStructure s) {
        Function<StyledText.Builder, StyledText.Builder> function = switch (s) {
            // [Group].[Attr] =
            case ObjectStructure.Attr(_, _, var attr, _) when attr.group().isPresent() -> b -> b
                .add(attr.group().orElseThrow() + '.', StyledFragment.NAME_DISABLED)
                .add(attr.name(), StyledFragment.NAME).add(" = ");

            // [Attr] =
            case ObjectStructure.Attr(_, _, var attr, _) -> b -> b
                .add(attr.name(), StyledFragment.NAME).add(" = ");

            // [Index] =
            case ObjectStructure.Index(_, _, _, int index) -> b -> b
                .add("[" + index + "]", StyledFragment.NAME)
                .add(" = ");

            default -> null;
        };
        return Optional.ofNullable(function);
    }

    private static Optional<Function<StyledText.Builder, StyledText.Builder>> valueTextBuilder(ObjectStructure s, boolean allowStyledText) {
        var value = s.value();
        if (value == null) {
            return Optional.of(b -> b.add("null"));
        }

        var type = s.type();
        var renderer = (Renderer<Object, Game>) null;
        if (s instanceof ObjectStructure.Attr(_, var clazz, var attr, _)) {
            renderer = Renderer.renderer(clazz, attr).orElse(null);
        }
        if (renderer == null) {
            renderer = Renderer.renderer(type).orElse(null);
        }
        if (renderer != null) {
            if (allowStyledText) {
                var styledText = renderer.styledText(type, value, s.game()).orElse(null);
                if (styledText != null) {
                    return Optional.of(tb -> tb.add(styledText));
                }
            }

            var text = renderer.text(type, value, s.game()).orElse(null);
            if (text != null) {
                return Optional.of(tb -> tb.add(text));
            }

            return Optional.empty();
        }

        if (type instanceof AtomTypeInfo || type instanceof EnumTypeInfo) {
            // Special case for primitive values; could become a dedicated renderer later
            return Optional.of(tb -> tb.add(String.valueOf(value)));
        }

        // Other types don't deserve a toString representation unless provided explicitly
        return Optional.empty();
    }
    // endregion

    // region Tooltip
    private static String getElementToolTip(ObjectStructure s) {
        var type = s.type();
        var buf = new StringBuilder();

        buf.append("<html><table>");
        switch (type) {
            case AtomTypeInfo i -> {
                appendSection(buf, "Atom");
                appendRow(buf, "Type", getTypeHierarchy(type, false));
                appendRow(buf, "Base", getTypeHierarchy(i.base(), false));
            }
            case EnumTypeInfo i -> {
                var value = (Value<?>) s.value();
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
        if (s instanceof ObjectStructure.Attr(_, _, var attr, _)) {
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
    // endregion

    private static class ObjectEditorTransferHandler extends TransferHandler {
        @Override
        protected Transferable createTransferable(JComponent c) {
            var tree = (StructuredTree<?>) c;
            if (tree.getSelectionPathComponent() instanceof ObjectStructure structure) {
                return getElementTransferable(structure).orElse(null);
            }
            return super.createTransferable(c);
        }

        @Override
        public int getSourceActions(JComponent c) {
            return COPY;
        }
    }

    private static class ObjectEditorLabelProvider implements StyledTreeLabelProvider<ObjectStructure> {
        @Override
        public Optional<StyledText> getStyledText(ObjectStructure element) {
            return Optional.of(getElementText(element));
        }

        @Override
        public Optional<Icon> getIcon(ObjectStructure element) {
            return Optional.of(Fugue.getIcon("blue-document"));
        }

        @Override
        public Optional<String> getToolTip(ObjectStructure element) {
            if (Application.getInstance().isDebugMode()) {
                return Optional.of(getElementToolTip(element));
            }
            return Optional.empty();
        }
    }

    private static class ObjectPreviewObjectProvider implements PreviewManager.PreviewObjectProvider {
        @Override
        public Optional<TypedObject> getObject(JTree tree, Object value) {
            return get(value);
        }

        @Override
        public Optional<TypeInfo> getType(JTree tree, Object value) {
            return get(value).map(TypedObject::getType);
        }

        private static Optional<TypedObject> get(Object value) {
            if (value instanceof ObjectStructure structure) {
                Object object = structure.value();
                if (object instanceof Ref<?> ref) {
                    // Should this be done here?
                    object = ref.get();
                }
                if (object instanceof TypedObject typed) {
                    return Optional.of(typed);
                }
            }
            return Optional.empty();
        }
    }
}
