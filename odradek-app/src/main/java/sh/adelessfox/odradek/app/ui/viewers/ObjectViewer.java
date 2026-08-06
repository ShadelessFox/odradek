package sh.adelessfox.odradek.app.ui.viewers;

import sh.adelessfox.odradek.NotImplementedException;
import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.component.PreviewManager;
import sh.adelessfox.odradek.app.ui.editors.ObjectEditorInput;
import sh.adelessfox.odradek.app.ui.editors.ObjectEditorInputLazy;
import sh.adelessfox.odradek.app.ui.viewers.menu.ObjectMenu;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.game.ObjectHolder;
import sh.adelessfox.odradek.game.decima.ObjectIdHolder;
import sh.adelessfox.odradek.game.decima.ObjectWithIdHolder;
import sh.adelessfox.odradek.rtti.*;
import sh.adelessfox.odradek.rtti.data.TypedObject;
import sh.adelessfox.odradek.rtti.data.Value;
import sh.adelessfox.odradek.rtti.util.TypePath;
import sh.adelessfox.odradek.ui.Focusable;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.Viewer;
import sh.adelessfox.odradek.ui.actions.Actions;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;
import sh.adelessfox.odradek.ui.components.tree.*;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.Optional;
import java.util.function.Function;

public final class ObjectViewer implements Viewer, Focusable {
    public static final class Provider implements Viewer.Provider<TypedObject> {
        @Override
        public Viewer create(TypedObject object, Game game, Optional<?> selection) {
            return new ObjectViewer(object, game, selection);
        }

        @Override
        public String name() {
            return "Object";
        }

        @Override
        public Optional<String> icon() {
            return Optional.of("fugue:blue-document");
        }
    }

    private final TypedObject object;
    private final Game game;
    private StructuredTree<ObjectStructure> tree;
    private Object selection;

    public ObjectViewer(TypedObject object, Game game, Optional<?> selection) {
        this.object = object;
        this.game = game;
        this.selection = selection.orElse(null);
    }

    @Override
    public JComponent createComponent() {
        tree = createObjectTree(game, object);
        return new JScrollPane(tree);
    }

    @Override
    public void activate() {
        if (selection instanceof TypePath path) {
            SwingUtilities.invokeLater(() -> selectPath(tree, path));
            selection = null;
        }
    }

    @Override
    public boolean isFocused() {
        return tree.isFocusOwner();
    }

    @Override
    public void setFocus() {
        tree.requestFocusInWindow();
    }

    private StructuredTree<ObjectStructure> createObjectTree(Game game, TypedObject object) {
        var tree = new StructuredTree<>(new ObjectStructure.Compound(game, object.getType(), object));
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setExpandsSelectedPaths(true);
        tree.setTransferHandler(new ObjectEditorTransferHandler());
        tree.setLabelProvider(new ObjectEditorLabelProvider());
        tree.addActionListener(TreeActionListener.treePathClickedAdapter(event -> {
            var component = event.getLastPathComponent();
            if (!(component instanceof ObjectStructure.Node node)) {
                return;
            }
            switch (node.value()) {
                case ObjectWithIdHolder<?> holder when holder.object() instanceof TypedObject typedObject -> {
                    var input = new ObjectEditorInput(game, typedObject, holder.objectId());
                    Application.getInstance().editors().openEditor(input);
                }
                case ObjectIdHolder holder -> {
                    var input = new ObjectEditorInputLazy(holder.objectId());
                    Application.getInstance().editors().openEditor(input);
                }
                case null, default -> {
                    // nothing to do
                }
            }
        }));
        Actions.installContextMenu(tree, ObjectMenu.ID, tree.or(key -> {
            if (DataKeys.GAME.is(key)) {
                return Optional.of(game);
            }
            return Optional.empty();
        }));
        PreviewManager.install(tree, game, new ObjectPreviewObjectProvider());

        installAutoExpandGroups(tree);
        expandTreeGroups(tree);

        return tree;
    }

    private static void installAutoExpandGroups(StructuredTree<ObjectStructure> tree) {
        tree.addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                expandTreeGroups(tree, event.getPath());
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                // nothing to do
            }
        });
    }

    private static void expandTreeGroups(StructuredTree<ObjectStructure> tree) {
        expandTreeGroups(tree, new TreePath(tree.getModel().getRoot()));
    }

    private static void expandTreeGroups(StructuredTree<ObjectStructure> tree, TreePath path) {
        if (!tree.isExpanded(path)) {
            return;
        }
        var model = tree.getModel();
        var parent = path.getLastPathComponent();
        for (int i = 0, count = model.getChildCount(parent); i < count; i++) {
            var child = model.getChild(parent, i);
            if (!(child.getValue() instanceof ObjectStructure.Group)) {
                continue;
            }
            var childPath = path.pathByAddingChild(child);
            if (!tree.hasBeenExpanded(childPath)) {
                tree.expandPath(childPath);
            }
        }
    }

    //region Paths
    private static void selectPath(StructuredTree<ObjectStructure> tree, TypePath selection) {
        findNode(tree, selection).ifPresent(path -> {
            tree.setSelectionPath(path);
            tree.scrollRowToVisible(Math.max(0, tree.getRowForPath(path) - 3 /* make some breathing room */));
        });
    }

    private static Optional<TreePath> findNode(StructuredTree<ObjectStructure> tree, TypePath selection) {
        var model = tree.getModel();
        var node = model.getRoot();
        var path = new TreePath(node);

        for (TypePath.Element element : selection.elements()) {
            Optional<TreeItem<ObjectStructure>> item = switch (element) {
                case TypePath.Element.Attr attr -> {
                    var group = attr.attr().group().orElse(null);
                    if (group == null) {
                        // No group, so we can just find the attribute directly
                        yield findAttrNode(model, node, attr);
                    }

                    // If the attribute is in a group, we need to find the group first, then the attribute
                    var group1 = findGroupNode(model, node, group).orElse(null);
                    if (group1 == null) {
                        yield Optional.empty();
                    }

                    path = path.pathByAddingChild(group1);
                    yield findAttrNode(model, group1, attr);
                }
                case TypePath.Element.Index index -> findIndexNode(model, node, index);
            };

            if (item.isPresent()) {
                node = item.orElseThrow();
                path = path.pathByAddingChild(node);
            } else {
                return Optional.empty();
            }
        }

        return Optional.of(path);
    }

    private static Optional<TreeItem<ObjectStructure>> findGroupNode(
        StructuredTreeModel<ObjectStructure> model,
        Object node,
        String group
    ) {
        return model.findChild(
            node,
            s -> s instanceof ObjectStructure.Group o
                && group.equals(o.name()));
    }

    private static Optional<TreeItem<ObjectStructure>> findAttrNode(
        StructuredTreeModel<ObjectStructure> model,
        Object node,
        TypePath.Element.Attr attr
    ) {
        return model.findChild(
            node,
            s -> s instanceof ObjectStructure.Attr o
                && attr.type() == o.info()
                && attr.attr() == o.attr());
    }

    private static Optional<TreeItem<ObjectStructure>> findIndexNode(
        StructuredTreeModel<ObjectStructure> model,
        Object node,
        TypePath.Element.Index index
    ) {
        return model.findChild(
            node,
            s -> s instanceof ObjectStructure.Index o
                && index.type() == o.info()
                && index.index() == o.index());
    }
    //endregion

    // region Text
    private static Optional<Transferable> getElementTransferable(ObjectStructure.Node s) {
        return valueTextBuilder(s, false)
            .map(b -> b.apply(StyledText.builder()))
            .flatMap(StyledText.Builder::build)
            .map(StyledText::toString)
            .map(StringSelection::new);
    }

    private static Optional<StyledText> getElementText(ObjectStructure.Node s) {
        var builder = StyledText.builder();
        keyTextBuilder(s).ifPresent(b -> b.apply(builder));
        builder.add("{" + s.type() + "} ", StyledFragment.GRAYED);
        valueTextBuilder(s, true).ifPresent(b -> b.apply(builder));
        return builder.build();
    }

    private static Optional<Function<StyledText.Builder, StyledText.Builder>> keyTextBuilder(ObjectStructure.Node s) {
        Function<StyledText.Builder, StyledText.Builder> function = switch (s) {
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

    private static Optional<Function<StyledText.Builder, StyledText.Builder>> valueTextBuilder(
        ObjectStructure.Node node,
        boolean allowStyledText
    ) {
        if (node.value() == null) {
            return Optional.of(b -> b.add("null"));
        }

        var type = node.type();
        var value = node.value();
        var renderer = (Renderer<Object, Game>) null;

        // Special handling for attributes
        if (node instanceof ObjectStructure.Attr(var game, var clazz, var attr, var object)) {
            renderer = Renderer.renderer(clazz, attr, game).orElse(null);

            // For attribute renderers, the parent type/object is passed
            if (renderer != null) {
                type = clazz;
                value = object;
            }
        }

        // If we couldn't find an attribute-specific renderer, try type-based renderer
        if (renderer == null) {
            renderer = Renderer.renderer(type, node.game()).orElse(null);
        }

        if (renderer != null) {
            if (allowStyledText) {
                var styledText = renderer.styledText(type, value, node.game()).orElse(null);
                if (styledText != null) {
                    return Optional.of(tb -> tb.add(styledText));
                }
            }

            var text = renderer.text(type, value, node.game()).orElse(null);
            if (text != null) {
                return Optional.of(tb -> tb.add(text));
            }

            return Optional.empty();
        }

        if (type instanceof AtomTypeInfo || type instanceof EnumTypeInfo) {
            // Special case for primitive values; SHOULD become a dedicated renderer later
            var text = String.valueOf(value);
            return Optional.of(tb -> tb.add(text));
        }

        // Other types don't deserve a toString representation unless provided explicitly
        return Optional.empty();
    }
    // endregion

    // region Tooltip
    private static String getElementToolTip(ObjectStructure.Node node) {
        var type = node.type();
        var buf = new StringBuilder();

        buf.append("<html><table>");
        switch (type) {
            case AtomTypeInfo i -> {
                appendSection(buf, "Atom");
                appendRow(buf, "Type", getTypeHierarchy(type, false));
                appendRow(buf, "Base", getTypeHierarchy(i.base(), false));
            }

            case EnumTypeInfo i -> {
                var value = (Value<?>) node.value();
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
            case BitSetTypeInfo _ -> throw new NotImplementedException(); // TODO
        }
        if (node instanceof ObjectStructure.Attr(_, _, var attr, _)) {
            appendSection(buf, "Attribute");
            appendRow(buf, "Offset", toText(attr.offset()));
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
            buf.repeat("&nbsp;&nbsp;&nbsp;&nbsp;", level - 1);
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
            if (tree.getSelectionPathComponent() instanceof ObjectStructure.Node node) {
                return getElementTransferable(node).orElse(null);
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
            return switch (element) {
                case ObjectStructure.Node node -> getElementText(node);
                case ObjectStructure.Group group -> StyledText.builder()
                    .add(group.name(), StyledFragment.BOLD.andThen(StyledFragment.GRAYED))
                    .build();
            };
        }

        @Override
        public Optional<Icon> getIcon(ObjectStructure element) {
            if (!(element instanceof ObjectStructure.Node)) {
                return Optional.empty();
            }
            return Optional.of(Fugue.getIcon("blue-document"));
        }

        @Override
        public Optional<String> getToolTip(ObjectStructure element) {
            if (!(element instanceof ObjectStructure.Node node)) {
                return Optional.empty();
            }
            var settings = Application.getInstance().settings();
            if (settings.showObjectTypeInformation().orElse(false)) {
                return Optional.of(getElementToolTip(node));
            }
            return Optional.empty();
        }
    }

    private static class ObjectPreviewObjectProvider implements PreviewManager.PreviewObjectProvider {
        @Override
        public Optional<TypeInfo> getType(JTree tree, Object value) {
            if (!Application.getInstance().settings().showObjectPreview().orElse(false)) {
                return Optional.empty();
            }
            return get(value).map(TypedObject::getType);
        }

        @Override
        public Optional<TypedObject> getObject(JTree tree, Object value) {
            return get(value);
        }

        private static Optional<TypedObject> get(Object value) {
            if (value instanceof ObjectStructure.Node node) {
                Object object = node.value();
                if (object instanceof ObjectHolder<?> holder) {
                    // Should this be done here?
                    object = holder.get();
                }
                if (object instanceof TypedObject typed) {
                    return Optional.of(typed);
                }
            }
            return Optional.empty();
        }
    }
}
