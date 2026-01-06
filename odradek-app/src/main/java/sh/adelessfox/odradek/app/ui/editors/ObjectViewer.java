package sh.adelessfox.odradek.app.ui.editors;

import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.component.PreviewManager;
import sh.adelessfox.odradek.app.ui.menu.object.ObjectMenu;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.game.hfw.rtti.data.StreamingLink;
import sh.adelessfox.odradek.game.hfw.rtti.data.StreamingRef;
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
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.Optional;
import java.util.function.Function;

public final class ObjectViewer implements Viewer {
    public static final class Provider implements Viewer.Provider<TypedObject> {
        @Override
        public Viewer create(TypedObject object, Game game) {
            return new ObjectViewer(object, game);
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

    public ObjectViewer(TypedObject object, Game game) {
        this.object = object;
        this.game = game;
    }

    @Override
    public JComponent createComponent() {
        return new JScrollPane(createObjectTree(game, object));
    }

    private StructuredTree<?> createObjectTree(Game game, TypedObject object) {
        var tree = new StructuredTree<>(new ObjectStructure.Compound(game, object.getType(), object));
        tree.setTransferHandler(new ObjectEditorTransferHandler());
        tree.setLabelProvider(new ObjectEditorLabelProvider());
        tree.addActionListener(event -> {
            var component = event.getLastPathComponent();
            if (!(component instanceof ObjectStructure structure)) {
                return;
            }
            switch (structure.value()) {
                case StreamingRef<?> ref -> {
                    var input = new ObjectEditorInputLazy(ref.groupId(), ref.objectIndex());
                    Application.getInstance().editors().openEditor(input);
                }
                case StreamingLink<?> link -> {
                    var input = new ObjectEditorInput(game, link.get(), link.groupId(), link.objectIndex());
                    Application.getInstance().editors().openEditor(input);
                }
                default -> {
                    // nothing to do
                }
            }
        });
        Actions.installContextMenu(tree, ObjectMenu.ID, tree.or(key -> {
            if (DataKeys.GAME.is(key)) {
                return Optional.of(game);
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
        var renderer = (sh.adelessfox.odradek.ui.Renderer<Object, Game>) null;
        if (s instanceof ObjectStructure.Attr(_, var clazz, var attr, _)) {
            renderer = sh.adelessfox.odradek.ui.Renderer.renderer(clazz, attr).orElse(null);
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
