package sh.adelessfox.odradek.app.ui.component.onboarding;

import com.formdev.flatlaf.util.UIScale;
import sh.adelessfox.odradek.app.ui.settings.ApplicationSettings;
import sh.adelessfox.odradek.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public final class OnboardingDialog extends JDialog {
    private OnboardingDialog(Window owner, Settings settings) {
        super(owner, "Getting started with Odradek", ModalityType.MODELESS);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        var panel = new OnboardingPanel(settings, this::dispose);
        setContentPane(panel);
        getRootPane().setDefaultButton(panel.defaultButton());
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "close-guide");
        getRootPane().getActionMap().put("close-guide", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        setMinimumSize(new Dimension(UIScale.scale(620), UIScale.scale(420)));
        setSize(UIScale.scale(850), UIScale.scale(550));
        setLocationRelativeTo(owner);
    }

    public static boolean shouldShowOnStartup(Settings settings) {
        return !settings.get(ApplicationSettings.GETTING_STARTED_SHOWN).value()
            || settings.get(ApplicationSettings.SHOW_GETTING_STARTED_ON_STARTUP).value();
    }

    /** Opens at most one guide per owner, preserving the page when it is already open. */
    public static void showGuide(Window owner, Settings settings) {
        for (var window : owner.getOwnedWindows()) {
            if (window instanceof OnboardingDialog dialog && dialog.isDisplayable()) {
                dialog.setVisible(true);
                dialog.toFront();
                dialog.requestFocus();
                return;
            }
        }
        var dialog = new OnboardingDialog(owner, settings);
        dialog.setVisible(true);
        settings.get(ApplicationSettings.GETTING_STARTED_SHOWN).set(true);
    }
}
