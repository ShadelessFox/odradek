package sh.adelessfox.odradek.ui.actions;

import sh.adelessfox.odradek.Gatherers;
import sh.adelessfox.odradek.ui.data.DataContext;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.function.Supplier;

public final class Actions {
    private static final List<ActionDescriptor> actions = loadActions();
    private static final Map<String, List<GroupDescriptor>> groups = loadGroups(actions);

    private Actions() {
    }

    public static JMenuBar createMenuBar(String id, DataContext context) {
        var group = groups.get(id).getFirst();
        var menuBar = new JMenuBar();

        for (ActionDescriptor action : group.actions()) {
            menuBar.add(createMenu(action, context));
        }

        return menuBar;
    }

    private static JMenu createMenu(ActionDescriptor action, DataContext context) {
        var name = TextWithMnemonic.parse(action.registration().name());

        var menu = new JMenu();
        menu.setText(name.text());
        menu.setMnemonic(name.mnemonicChar());
        menu.setDisplayedMnemonicIndex(name.mnemonicIndex());

        var popupMenu = menu.getPopupMenu();
        popupMenu.addPopupMenuListener(new ActionPopupMenuListener(null, popupMenu, action, context));

        return menu;
    }

    private static JMenuItem createMenuItem(MenuItemAction action) {
        if (groups.containsKey(action.descriptor.registration().id())) {
            var menu = new JMenu(action);
            var popupMenu = menu.getPopupMenu();
            popupMenu.addPopupMenuListener(new ActionPopupMenuListener(null, popupMenu, action.descriptor, action.context));
            return menu;
        } else {
            return new JMenuItem(action);
        }
    }

    private static void populateMenu(JPopupMenu menu, ActionDescriptor descriptor, ActionContext context) {
        var groups = Actions.groups.get(descriptor.registration().id());
        if (groups == null || groups.isEmpty()) {
            return;
        }
        for (GroupDescriptor group : groups) {
            populateMenuGroup(menu, group, context);
        }
        if (menu.getComponentCount() == 0) {
            menu.add(new JLabel("No actions"));
        }
    }

    private static void populateMenuGroup(JPopupMenu menu, GroupDescriptor descriptor, ActionContext context) {
        var actions = descriptor.actions().stream()
            .filter(d -> d.action().isVisible(context))
            .toList();
        if (actions.isEmpty()) {
            return;
        }
        if (menu.getComponentCount() > 0) {
            menu.addSeparator();
        }
        for (ActionDescriptor action : actions) {
            menu.add(createMenuItem(new MenuItemAction(action, context)));
        }
    }

    private static List<ActionDescriptor> loadActions() {
        return ServiceLoader.load(Action.class).stream()
            .map(provider -> ActionDescriptor.unreflect(provider.type(), provider))
            .gather(Gatherers.ensureDistinct(
                d -> d.registration().id(),
                d -> new IllegalArgumentException("Duplicate action ID: " + d.registration().id() + " for " + d.action().getClass())))
            .toList();
    }

    private static Map<String, List<GroupDescriptor>> loadGroups(List<ActionDescriptor> actions) {
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
                        .sorted(Comparator.comparingInt(action -> action.contribution().order()))
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
            var name = TextWithMnemonic.parse(descriptor.registration().name());

            putValue(NAME, name.text());
            putValue(MNEMONIC_KEY, name.mnemonicChar());
            putValue(DISPLAYED_MNEMONIC_INDEX_KEY, name.mnemonicIndex());
            putValue(SHORT_DESCRIPTION, descriptor.registration().description());

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

    private record ActionPopupMenuListener(Object source, JPopupMenu popupMenu, ActionDescriptor descriptor, DataContext context) implements PopupMenuListener {
        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            populateMenu(popupMenu, descriptor, new ActionContext(context, source, null));
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

    private record ActionDescriptor(Action action, ActionRegistration registration, List<ActionContribution> contributions) {
        static ActionDescriptor unreflect(Class<? extends Action> type, Supplier<? extends Action> supplier) {
            var registration = type.getDeclaredAnnotation(ActionRegistration.class);
            var contributions = type.getDeclaredAnnotationsByType(ActionContribution.class);

            if (registration == null) {
                throw new IllegalArgumentException("Action class " + type.getName() + " is not annotated with @ActionRegistration");
            }

            return new ActionDescriptor(
                supplier.get(),
                registration,
                List.of(contributions)
            );
        }
    }

    private record GroupDescriptor(String id, int order, List<ActionDescriptor> actions) {
    }
}
