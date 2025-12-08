package sh.adelessfox.odradek.ui.editors.lazy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.ui.editors.Editor;
import sh.adelessfox.odradek.ui.editors.EditorInput;
import sh.adelessfox.odradek.ui.editors.EditorManager;
import sh.adelessfox.odradek.ui.editors.EditorSite;
import sh.adelessfox.odradek.ui.util.LoadingIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.util.concurrent.CancellationException;

public class LazyEditor implements Editor {
    private static final Logger log = LoggerFactory.getLogger(LazyEditor.class);
    private final LazyEditorInput input;
    private final EditorSite site;

    private final LoadingIcon icon;
    private final JLabel label;
    private final JButton button;
    private final LoadingWorker worker;
    private final Timer timer;

    private boolean focusRequested;

    public LazyEditor(LazyEditorInput input, EditorSite site) {
        this.input = input;
        this.site = site;

        this.icon = new LoadingIcon();
        this.label = new JLabel("Editor is not initialized", SwingConstants.CENTER);
        this.button = new JButton("Initialize");
        this.button.addActionListener(_ -> site.getManager().openEditor(this, input.canLoadImmediately(true)));

        this.worker = new LoadingWorker();
        this.timer = LoadingIcon.createTimer(label);
    }

    @Override
    public JComponent createComponent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(5, 5));
        panel.add(label, BorderLayout.NORTH);
        panel.add(button, BorderLayout.CENTER);

        // Fixes foreground color when changing themes
        label.updateUI();

        JPanel host = new JPanel();
        host.setLayout(new GridBagLayout());
        host.add(panel);

        if (input.canLoadImmediately()) {
            initialize();
        }

        return host;
    }

    @Override
    public EditorInput getInput() {
        return input;
    }

    @Override
    public void setFocus() {
        focusRequested = true;
        button.requestFocusInWindow();
    }

    @Override
    public boolean isFocused() {
        return focusRequested;
    }

    @Override
    public void dispose() {
        timer.stop();
        worker.cancel(true);
    }

    private void initialize() {
        label.setText("Loading\u2026");
        label.setIcon(icon);
        label.addHierarchyListener(e -> {
            if (e.getID() == HierarchyEvent.HIERARCHY_CHANGED && (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (label.isShowing()) {
                    timer.start();
                } else {
                    timer.stop();
                }
            }
        });

        button.setVisible(false);
        worker.execute();
    }

    private class LoadingWorker extends SwingWorker<EditorInput, Void> {
        @Override
        protected EditorInput doInBackground() throws Exception {
            return input.loadRealInput();
        }

        @Override
        protected void done() {
            if (isCancelled()) {
                return;
            }
            EditorManager manager = site.getManager();
            try {
                manager.openEditor(LazyEditor.this, get());
            } catch (InterruptedException | CancellationException e) {
                manager.openEditor(LazyEditor.this, input.canLoadImmediately(false));
            } catch (Throwable e) {
                manager.openEditor(LazyEditor.this, input.canLoadImmediately(false));
                log.error("Unable to open editor for '{}'", input.getName());
                // UIUtils.showErrorDialog(e, "Unable to open editor for '%s'".formatted(input.getName()));
            }
        }
    }
}
