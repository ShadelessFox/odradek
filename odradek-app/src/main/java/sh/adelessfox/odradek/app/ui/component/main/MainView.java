package sh.adelessfox.odradek.app.ui.component.main;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.app.ui.component.common.View;
import sh.adelessfox.odradek.app.ui.settings.ApplicationSettings;
import sh.adelessfox.odradek.app.ui.tools.graph.GraphToolPanel;
import sh.adelessfox.odradek.data.DataContext;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.settings.SettingsEvent;
import sh.adelessfox.odradek.ui.editors.EditorManager;
import sh.adelessfox.odradek.ui.tools.ToolContainer;
import sh.adelessfox.odradek.ui.tools.ToolPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

@Singleton
public class MainView implements View<JComponent> {
    private final JPanel root;

    @Inject
    MainView(
        EditorManager editorManager,
        EventBus eventBus,
        DataContext context
    ) {
        var center = buildCenter(editorManager, eventBus, context);
        var bottom = buildBottom();

        root = new JPanel(new BorderLayout());
        root.add(center);
        root.add(bottom, BorderLayout.SOUTH);
    }

    @Override
    public JComponent getRoot() {
        return root;
    }

    private static JComponent buildCenter(
        EditorManager editorManager,
        EventBus eventBus,
        DataContext context
    ) {
        var center = new ToolContainer(context);
        ToolPanel.providers().forEach(provider -> center.addPanel(provider, provider.placement()));
        center.setCenter(editorManager.getRoot());

        // The initial state
        center.openPanel(GraphToolPanel.ID, true);
        center.setWeight(ToolPanel.Placement.Anchor.LEFT, 0.2);
        center.setWeight(ToolPanel.Placement.Anchor.BOTTOM, 0.6);
        center.setToolNamesVisible(true);

        eventBus.subscribe(MainEvent.ShowPanel.class, event -> center.openPanel(event.id(), event.focus()));
        eventBus.subscribe(SettingsEvent.class, event -> {
            switch (event) {
                case SettingsEvent.AfterLoad(var settings) ->
                    settings.get(ApplicationSettings.TOOLS).value().ifPresent(center::setState);
                case SettingsEvent.BeforeSave(var settings) ->
                    settings.get(ApplicationSettings.TOOLS).set(Optional.of(center.getState()));
            }
        });

        return center.getComponent();
    }

    private static Component buildBottom() {
        return Box.createVerticalStrut(5);
    }
}
