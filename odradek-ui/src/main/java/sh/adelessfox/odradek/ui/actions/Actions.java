package sh.adelessfox.odradek.ui.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.Gatherers;
import sh.adelessfox.odradek.ui.data.DataContext;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    @SuppressWarnings("unchecked")
    public static void installContextMenu(JComponent component, String id, DataContext context) {
        var popupMenu = createPopupMenu(component, id, context);
        var selectionProvider = switch (component) {
            case JTabbedPane _ -> SelectionProvider.tabbedPaneSelection();
            case JTree _ -> SelectionProvider.treeSelection();
            default -> SelectionProvider.componentSelection();
        };
        installContextMenu(component, popupMenu, (SelectionProvider<JComponent, ?>) selectionProvider);
        DataContext.putDataContext(component, context);
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
                    selectionProvider.getSelection(component, e).ifPresent(selection -> {
                        var location = selectionProvider.getSelectionLocation(component, selection, e);
                        popupMenu.show(component, location.x, location.y);
                    });
                }
            }
        });

        var action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectionProvider.getSelection(component, e).ifPresent(selection -> {
                    var location = selectionProvider.getSelectionLocation(component, selection, e);
                    popupMenu.show(component, location.x, location.y);
                });
            }
        };

        InputMap inputMap = component.getInputMap(JComponent.WHEN_FOCUSED);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0), action);

        ActionMap actionMap = component.getActionMap();
        actionMap.put(action, action);
    }

    public static JPopupMenu createPopupMenu(JComponent component, String id, DataContext context) {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.addPopupMenuListener(new ActionPopupMenuListener(component, popupMenu, id, context));
        return popupMenu;
    }

    public static JMenuBar createMenuBar(String id, DataContext context) {
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

    private static JMenu createMenu(ActionDescriptor action, DataContext context) {
        var name = TextWithMnemonic.parse(action.text(new ActionContext(context, null, null)));

        var menu = new JMenu();
        menu.setText(name.text());
        menu.setMnemonic(name.mnemonicChar());
        menu.setDisplayedMnemonicIndex(name.mnemonicIndex());

        var popupMenu = menu.getPopupMenu();
        popupMenu.addPopupMenuListener(new ActionPopupMenuListener(null, popupMenu, action.id(), context));

        return menu;
    }

    private static JMenuItem createMenuItem(MenuItemAction action) {
        if (groups.containsKey(action.descriptor.id())) {
            var menu = new JMenu(action);
            var popupMenu = menu.getPopupMenu();
            popupMenu.addPopupMenuListener(new ActionPopupMenuListener(null, popupMenu, action.descriptor.id(), action.context));
            return menu;
        } else if (action.descriptor.action() instanceof Action.Check) {
            return new JCheckBoxMenuItem(action);
        } else if (action.descriptor.action() instanceof Action.Radio) {
            return new JRadioButtonMenuItem(action);
        } else {
            return new JMenuItem(action);
        }
    }

    private static void populateMenu(JPopupMenu menu, String id, ActionContext context) {
        var groups = Actions.groups.get(id);
        if (groups == null || groups.isEmpty()) {
            log.debug("No action groups found for ID: {}", id);
            return;
        }
        for (GroupDescriptor group : groups) {
            populateMenuGroup(menu, group, context);
        }
        if (menu.getComponentCount() == 0) {
            menu.add(new DisabledAction("No actions"));
        }
    }

    private static void populateMenuGroup(JPopupMenu menu, ActionDescriptorProvider provider, ActionContext context) {
        var actions = provider.create(context);
        if (actions.isEmpty()) {
            return;
        }
        if (menu.getComponentCount() > 0) {
            menu.addSeparator();
        }
        for (ActionDescriptor action : actions) {
            if (action.action() instanceof ActionProvider p) {
                populateMenuGroup(menu, ActionDescriptorProvider.wrap(p), context);
            } else {
                menu.add(createMenuItem(new MenuItemAction(action, context)));
            }
        }
    }

    private static Map<String, ActionDescriptor> loadActions() {
        return ServiceLoader.load(Action.class).stream()
            .map(provider -> ActionDescriptor.unreflect(provider.type(), provider))
            .gather(Gatherers.ensureDistinct(
                ActionDescriptor::id,
                d -> new IllegalArgumentException("Duplicate action ID: " + d.id() + " for " + d.action().getClass())))
            .collect(Collectors.toMap(
                ActionDescriptor::id,
                Function.identity()));
    }

    private static Map<String, List<GroupDescriptor>> loadGroups(Collection<ActionDescriptor> actions) {
        record ActionInfo(ActionDescriptor action, ActionContribution contribution) {}
        record GroupInfo(String id, int order, List<ActionInfo> actions) {}

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
        public void actionPerformed(ActionEvent e) {
            if (descriptor.action().isEnabled(context)) {
                perform(e);
            }
        }

        protected abstract void perform(ActionEvent e);

        private void update() {
            var name = TextWithMnemonic.parse(descriptor.text(context));
            putValue(NAME, name.text());
            putValue(MNEMONIC_KEY, name.mnemonicChar());
            putValue(DISPLAYED_MNEMONIC_INDEX_KEY, name.mnemonicIndex());
            putValue(SHORT_DESCRIPTION, descriptor.description(context));

            var action = descriptor.action();
            if (action instanceof Action.Check check) {
                putValue(SELECTED_KEY, check.isChecked(context));
            } else if (action instanceof Action.Radio radio) {
                putValue(SELECTED_KEY, radio.isSelected(context));
            }

            setEnabled(descriptor.action().isEnabled(context));
        }
    }

    private static class MenuItemAction extends AbstractMenuAction {
        MenuItemAction(ActionDescriptor descriptor, ActionContext context) {
            super(descriptor, context);
        }

        @Override
        protected void perform(ActionEvent e) {
            descriptor.action().perform(context.withEvent(e));
        }
    }

    private record ActionPopupMenuListener(Object source, JPopupMenu popupMenu, String id, DataContext context) implements PopupMenuListener {
        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            populateMenu(popupMenu, id, new ActionContext(context, source, null));
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
        Function<ActionContext, String> textSupplier,
        Function<ActionContext, String> descriptionSupplier,
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
            Function<ActionContext, String> textSupplier = c -> action.getText(c).orElse(registration.text());
            Function<ActionContext, String> descriptionSupplier = c -> action.getDescription(c).orElse(registration.description());

            return new ActionDescriptor(
                action,
                id,
                textSupplier,
                descriptionSupplier,
                List.of(contributions)
            );
        }

        static ActionDescriptor wrap(Action action) {
            var id = action.getClass().getName();
            Function<ActionContext, String> textSupplier = c -> action.getText(c).orElse("");
            Function<ActionContext, String> descriptionSupplier = c -> action.getDescription(c).orElse("");

            return new ActionDescriptor(
                action,
                id,
                textSupplier,
                descriptionSupplier,
                List.of()
            );
        }

        String text(ActionContext context) {
            return textSupplier.apply(context);
        }

        String description(ActionContext context) {
            return descriptionSupplier.apply(context);
        }
    }

    private record GroupDescriptor(String id, int order, List<ActionDescriptor> actions) implements ActionDescriptorProvider {
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
}
