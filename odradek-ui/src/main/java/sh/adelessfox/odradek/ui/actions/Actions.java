package sh.adelessfox.odradek.ui.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.ui.data.DataContext;
import sh.adelessfox.odradek.ui.util.Icons;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class Actions {
    private static final Map<String, ActionDescriptor> actions = loadActions();
    private static final Map<String, List<GroupDescriptor>> groups = loadGroups(actions.values());
    private static final Logger log = LoggerFactory.getLogger(Actions.class);

    private Actions() {
    }

    public static void installMenuBar(JRootPane pane, String id, DataContext context) {
        pane.setJMenuBar(createMenuBar(id, context));
        populateActionBindings(pane, id, new ActionContext(context, pane, null), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    @SuppressWarnings("unchecked")
    public static void installContextMenu(JComponent component, String id, DataContext context) {
        var popupMenu = createPopupMenu(component, id, context, false);
        var selectionProvider = getSelectionProvider(component);
        installContextMenu(component, popupMenu, (SelectionProvider<JComponent, ?>) selectionProvider);
        populateActionBindings(component, id, new ActionContext(context, component, null), JComponent.WHEN_FOCUSED);
        DataContext.putDataContext(component, context);
    }

    public static JToolBar createToolBar(String id, DataContext context) {
        var toolBar = new JToolBar();
        contributeGroups(toolBar, new ToolBarActionContributor(), id, new ActionContext(context, null, null));
        return toolBar;
    }

    private static <T extends JComponent, R> void installContextMenu(T component, JPopupMenu popupMenu, SelectionProvider<? super T, R> selectionProvider) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    selectionProvider.getSelection(component, e).ifPresent(selection -> {
                        selectionProvider.setSelection(component, selection, e);
                    });
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showPopupMenu(component, popupMenu, e, selectionProvider);
                }
            }
        });

        var action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPopupMenu(component, popupMenu, e, selectionProvider);
            }
        };

        InputMap inputMap = component.getInputMap(JComponent.WHEN_FOCUSED);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0), action);

        ActionMap actionMap = component.getActionMap();
        actionMap.put(action, action);
    }

    private static JPopupMenu createPopupMenu(JComponent component, String id, DataContext context, boolean includeSourceAction) {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.addPopupMenuListener(new ActionPopupMenuListener(component, popupMenu, id, context, includeSourceAction));
        return popupMenu;
    }

    private static JMenuBar createMenuBar(String id, DataContext context) {
        var groups = Actions.groups.get(id);
        if (groups == null) {
            throw new IllegalArgumentException("No action groups found for ID: " + id);
        }
        if (groups.size() != 1) {
            throw new IllegalArgumentException("Expected exactly one action group for ID: " + id + ", found: " + groups.size());
        }

        var group = groups.getFirst();
        var menuBar = new JMenuBar();

        for (ActionDescriptor action : group.actions()) {
            menuBar.add(createMenu(action, context));
        }

        return menuBar;
    }

    private static void populateActionBindings(JComponent component, String id, ActionContext context, int condition) {
        var groups = Actions.groups.get(id);
        if (groups == null || groups.isEmpty()) {
            return;
        }

        for (GroupDescriptor group : groups) {
            for (ActionDescriptor action : group.actions()) {
                populateActionBindings(component, action.id(), context, condition);
                populateActionBinding(component, action, context, condition);
            }
        }
    }

    @SuppressWarnings("MagicConstant")
    private static void populateActionBinding(JComponent component, ActionDescriptor descriptor, ActionContext context, int condition) {
        if (descriptor.accelerator() == null) {
            return;
        }

        AbstractMenuAction menuAction;
        if (groups.containsKey(descriptor.id())) {
            menuAction = new PopupMenuAction(component, descriptor, context, true);
        } else {
            menuAction = new MenuItemAction(descriptor, context);
        }

        ActionMap actionMap = component.getActionMap();
        actionMap.put(descriptor, menuAction);

        InputMap inputMap = component.getInputMap(condition);
        inputMap.put(descriptor.accelerator(), descriptor);
    }

    private static JMenu createMenu(ActionDescriptor action, DataContext context) {
        var name = action.text(new ActionContext(context, null, null));

        var menu = new JMenu();
        menu.setText(name.text());
        menu.setMnemonic(name.mnemonicChar());
        menu.setDisplayedMnemonicIndex(name.mnemonicIndex());

        var popupMenu = menu.getPopupMenu();
        popupMenu.addPopupMenuListener(new ActionPopupMenuListener(null, popupMenu, action.id(), context, false));

        return menu;
    }

    private static void populateMenu(JPopupMenu menu, String id, ActionContext context, boolean includeSourceAction) {
        var groups = Actions.groups.get(id);
        if (groups == null || groups.isEmpty()) {
            log.debug("No action groups found for ID: {}", id);
            return;
        }
        if (includeSourceAction) {
            var action = actions.get(id);
            var name = action.text(context);
            menu.add(new DisabledAction(name.text()));
            menu.addSeparator();
        }
        boolean contributed = contributeGroups(menu, new PopupMenuActionContributor(), id, context);
        if (!contributed) {
            menu.add(new DisabledAction("No actions"));
        }
    }

    private static <T extends JComponent, R> void showPopupMenu(
        T component,
        JPopupMenu popupMenu,
        EventObject event,
        SelectionProvider<? super T, R> selectionProvider
    ) {
        selectionProvider.getSelection(component, event).ifPresent(selection -> {
            var location = selectionProvider.getSelectionLocation(component, selection, event);
            popupMenu.show(component, location.x, location.y);
        });
    }

    private static SelectionProvider<? extends JComponent, ?> getSelectionProvider(JComponent component) {
        return switch (component) {
            case JTabbedPane _ -> SelectionProvider.tabbedPaneSelection();
            case JTree _ -> SelectionProvider.treeSelection();
            default -> SelectionProvider.componentSelection();
        };
    }

    private static Map<String, ActionDescriptor> loadActions() {
        return ServiceLoader.load(Action.class).stream()
            .map(provider -> ActionDescriptor.unreflect(provider.type(), provider))
            .collect(Collectors.toMap(
                ActionDescriptor::id,
                Function.identity(),
                (a, b) -> {
                    throw new IllegalArgumentException("Duplicate action ID '" + a.id()
                        + "': conflicting definitions are " + a.action().getClass().getName()
                        + " and " + b.action().getClass().getName());
                }
            ));
    }

    private static <C extends JComponent> boolean contributeGroups(
        C component,
        ActionContributor<C, ?> contributor,
        String id,
        ActionContext context
    ) {
        var groups = Actions.groups.get(id);
        if (groups == null || groups.isEmpty()) {
            log.debug("No action groups found for ID: {}", id);
            return false;
        }
        boolean contributed = false;
        for (GroupDescriptor group : groups) {
            contributed |= contributeGroup(component, contributor, group, context, contributed);
        }
        return contributed;
    }

    private static <C, T> boolean contributeGroup(
        C component,
        ActionContributor<C, T> contributor,
        ActionDescriptorProvider provider,
        ActionContext context,
        boolean contributed
    ) {
        var actions = provider.create(context);
        if (actions.isEmpty()) {
            return false;
        }
        if (contributed) {
            contributor.addSeparator(component);
        }
        for (ActionDescriptor action : actions) {
            if (action.action() instanceof ActionProvider p) {
                contributed |= contributeGroup(component, contributor, ActionDescriptorProvider.wrap(p), context, contributed);
            } else {
                contributor.addItem(component, createGroupItem(component, contributor, new MenuItemAction(action, context)));
                contributed = true;
            }
        }
        return contributed;
    }

    private static <C, T> T createGroupItem(C component, ActionContributor<C, T> contributor, MenuItemAction action) {
        if (groups.containsKey(action.descriptor.id())) {
            return contributor.createPopupItem(component, action);
        } else if (action.descriptor.action() instanceof Action.Check) {
            return contributor.createCheckItem(component, action);
        } else if (action.descriptor.action() instanceof Action.Radio) {
            return contributor.createRadioItem(component, action);
        } else {
            return contributor.createItem(component, action);
        }
    }


    private static Map<String, List<GroupDescriptor>> loadGroups(Collection<ActionDescriptor> actions) {
        record ActionInfo(ActionDescriptor action, ActionContribution contribution) {
        }

        record GroupInfo(String id, int order, List<ActionInfo> actions) {
        }

        Map<String, Map<String, GroupInfo>> collected = new HashMap<>();
        for (ActionDescriptor action : actions) {
            for (ActionContribution contribution : action.contributions()) {
                var group = contribution.group().split(",");
                var groupId = group[1];
                var groupOrder = Integer.parseInt(group[0]);

                GroupInfo info = collected
                    .computeIfAbsent(contribution.parent(), _ -> new HashMap<>())
                    .computeIfAbsent(groupId, _ -> new GroupInfo(groupId, groupOrder, new ArrayList<>()));

                info.actions().add(new ActionInfo(action, contribution));
            }
        }

        Map<String, List<GroupDescriptor>> result = new HashMap<>();
        for (var entry : collected.entrySet()) {
            List<GroupDescriptor> descriptors = entry.getValue().values().stream()
                .sorted(Comparator.comparingInt(GroupInfo::order))
                .map(group -> new GroupDescriptor(
                    group.id(),
                    group.order(),
                    group.actions().stream()
                        .sorted(Comparator.comparingInt((ActionInfo action) -> action.contribution().order()))
                        .map(ActionInfo::action)
                        .toList()))
                .toList();

            result.put(entry.getKey(), descriptors);
        }

        return Map.copyOf(result);
    }

    private static abstract class AbstractMenuAction extends AbstractAction {
        protected final ActionDescriptor descriptor;
        protected final ActionContext context;

        AbstractMenuAction(ActionDescriptor descriptor, ActionContext context) {
            this.descriptor = descriptor;
            this.context = context;
            update();
        }

        @Override
        public boolean accept(Object sender) {
            return descriptor.action().isEnabled(context);
        }

        private void update() {
            var name = descriptor.text(context);
            var text = name.text();
            if (descriptor.accelerator() != null && groups.containsKey(descriptor.id())) {
                // Menus with sub-menus can't have an accelerator in Swing, so we have to fake it
                text += "\u3000\u3000\u3000";
                text += getTextForAccelerator(descriptor.accelerator());
            }
            putValue(NAME, text);
            putValue(MNEMONIC_KEY, name.mnemonicChar());
            putValue(DISPLAYED_MNEMONIC_INDEX_KEY, name.mnemonicIndex());
            putValue(SHORT_DESCRIPTION, descriptor.description(context));
            putValue(SMALL_ICON, descriptor.icon(context));
            putValue(ACCELERATOR_KEY, descriptor.accelerator());

            var action = descriptor.action();
            if (action instanceof Action.Check check) {
                putValue(SELECTED_KEY, check.isChecked(context));
            } else if (action instanceof Action.Radio radio) {
                putValue(SELECTED_KEY, radio.isSelected(context));
            }

            setEnabled(descriptor.action().isEnabled(context));
        }

        /**
         * Copied from {@code com.formdev.flatlaf.ui.FlatMenuItemRenderer#getTextForAccelerator(KeyStroke)}
         */
        private static String getTextForAccelerator(KeyStroke accelerator) {
            StringBuilder buf = new StringBuilder();

            // modifiers
            int modifiers = accelerator.getModifiers();
            if (modifiers != 0) {
                buf.append(InputEvent.getModifiersExText(modifiers)).append('+');
            }

            // key
            int keyCode = accelerator.getKeyCode();
            if (keyCode != 0) {
                buf.append(KeyEvent.getKeyText(keyCode));
            } else {
                buf.append(accelerator.getKeyChar());
            }

            return buf.toString();
        }
    }

    private static class MenuItemAction extends AbstractMenuAction {
        MenuItemAction(ActionDescriptor descriptor, ActionContext context) {
            super(descriptor, context);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            descriptor.action().perform(context.withEvent(e));
        }
    }

    private static class PopupMenuAction extends AbstractMenuAction {
        private final JComponent component;
        private final boolean includeSourceAction;

        public PopupMenuAction(
            JComponent component,
            ActionDescriptor descriptor,
            ActionContext context,
            boolean includeSourceAction
        ) {
            this.includeSourceAction = includeSourceAction;
            this.component = component;
            super(descriptor, context);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void actionPerformed(ActionEvent e) {
            var popupMenu = createPopupMenu(component, descriptor.id(), context, includeSourceAction);
            var selectionProvider = getSelectionProvider(component);
            showPopupMenu(component, popupMenu, e, (SelectionProvider<JComponent, ?>) selectionProvider);
        }
    }

    private record ActionPopupMenuListener(
        Object source,
        JPopupMenu popupMenu,
        String id,
        DataContext context,
        boolean includeSourceAction
    ) implements PopupMenuListener {
        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            populateMenu(popupMenu, id, new ActionContext(context, source, null), includeSourceAction);
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            popupMenu.removeAll();
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
            popupMenu.removeAll();
        }
    }

    private record ActionDescriptor(
        Action action,
        String id,
        KeyStroke accelerator,
        Function<ActionContext, String> textSupplier,
        Function<ActionContext, String> descriptionSupplier,
        Function<ActionContext, Icon> iconSupplier,
        List<ActionContribution> contributions
    ) {
        static ActionDescriptor unreflect(Class<? extends Action> type, Supplier<? extends Action> supplier) {
            var registration = type.getDeclaredAnnotation(ActionRegistration.class);
            var contributions = type.getDeclaredAnnotationsByType(ActionContribution.class);

            if (registration == null) {
                throw new IllegalArgumentException("Action " + type + " is not annotated with @ActionRegistration");
            }

            var action = supplier.get();
            var id = registration.id().isEmpty() ? type.getName() : registration.id();
            var accelerator = registration.keystroke().isEmpty() ? null : KeyStroke.getKeyStroke(registration.keystroke());

            Function<ActionContext, String> textSupplier = c -> action.getText(c).orElse(registration.text());
            Function<ActionContext, String> descriptionSupplier = c -> action.getDescription(c).orElse(registration.description());
            Function<ActionContext, Icon> iconSupplier = c -> Icons.getIconFromUri(action.getIcon(c).orElse(registration.icon())).orElse(null);

            return new ActionDescriptor(
                action,
                id,
                accelerator,
                textSupplier,
                descriptionSupplier,
                iconSupplier,
                List.of(contributions)
            );
        }

        static ActionDescriptor wrap(Action action) {
            var id = action.getClass().getName();
            Function<ActionContext, String> textSupplier = c -> action.getText(c).orElse("");
            Function<ActionContext, String> descriptionSupplier = c -> action.getDescription(c).orElse("");
            Function<ActionContext, Icon> iconSupplier = c -> action.getIcon(c).flatMap(Icons::getIconFromUri).orElse(null);

            return new ActionDescriptor(
                action,
                id,
                null,
                textSupplier,
                descriptionSupplier,
                iconSupplier,
                List.of()
            );
        }

        TextWithMnemonic text(ActionContext context) {
            return TextWithMnemonic.parse(textSupplier.apply(context));
        }

        String description(ActionContext context) {
            return descriptionSupplier.apply(context);
        }

        Icon icon(ActionContext context) {
            return iconSupplier.apply(context);
        }
    }

    private record GroupDescriptor(
        String id,
        int order,
        List<ActionDescriptor> actions
    ) implements ActionDescriptorProvider {
        @Override
        public List<ActionDescriptor> create(ActionContext context) {
            return actions.stream()
                .filter(action -> action.action().isVisible(context))
                .toList();
        }
    }

    private interface ActionDescriptorProvider {
        static ActionDescriptorProvider wrap(ActionProvider provider) {
            return context -> provider.create(context).stream()
                .map(ActionDescriptor::wrap)
                .toList();
        }

        List<ActionDescriptor> create(ActionContext context);
    }

    private static class PopupMenuActionContributor implements ActionContributor<JPopupMenu, JMenuItem> {
        @Override
        public void addSeparator(JPopupMenu component) {
            component.addSeparator();
        }

        @Override
        public void addItem(JPopupMenu component, JMenuItem item) {
            component.add(item);
        }

        @Override
        public JMenuItem createPopupItem(JPopupMenu component, AbstractMenuAction action) {
            var menu = new JMenu(action);
            var popupMenu = menu.getPopupMenu();
            popupMenu.addPopupMenuListener(new ActionPopupMenuListener(null, popupMenu, action.descriptor.id(), action.context, false));
            return menu;
        }

        @Override
        public JMenuItem createCheckItem(JPopupMenu component, AbstractMenuAction action) {
            return new JCheckBoxMenuItem(action);
        }

        @Override
        public JMenuItem createRadioItem(JPopupMenu component, AbstractMenuAction action) {
            return new JRadioButtonMenuItem(action);
        }

        @Override
        public JMenuItem createItem(JPopupMenu component, AbstractMenuAction action) {
            return new JMenuItem(action);
        }
    }

    private static class ToolBarActionContributor implements ActionContributor<JToolBar, AbstractAction> {
        @Override
        public void addSeparator(JToolBar component) {
            component.addSeparator();
        }

        @Override
        public void addItem(JToolBar component, AbstractAction item) {
            component.add(item);
        }

        @Override
        public AbstractAction createPopupItem(JToolBar component, AbstractMenuAction action) {
            return new PopupMenuAction(component, action.descriptor, action.context, false);
        }

        @Override
        public AbstractAction createCheckItem(JToolBar component, AbstractMenuAction action) {
            throw new IllegalStateException("Check actions are not supported in toolbars");
        }

        @Override
        public AbstractAction createRadioItem(JToolBar component, AbstractMenuAction action) {
            throw new IllegalStateException("Radio actions are not supported in toolbars");
        }

        @Override
        public AbstractAction createItem(JToolBar component, AbstractMenuAction action) {
            return action;
        }
    }

    private interface ActionContributor<C, T> {
        void addSeparator(C component);

        void addItem(C component, T item);

        T createPopupItem(C component, AbstractMenuAction action);

        T createCheckItem(C component, AbstractMenuAction action);

        T createRadioItem(C component, AbstractMenuAction action);

        T createItem(C component, AbstractMenuAction action);
    }
}
