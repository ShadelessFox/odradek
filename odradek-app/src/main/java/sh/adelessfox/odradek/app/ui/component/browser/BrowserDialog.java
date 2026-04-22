package sh.adelessfox.odradek.app.ui.component.browser;

import net.miginfocom.swing.MigLayout;
import sh.adelessfox.odradek.ui.components.tree.StructuredTree;
import sh.adelessfox.odradek.ui.components.tree.TreeActionEvent;
import sh.adelessfox.odradek.ui.components.tree.TreeActionListener;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.Optional;

public final class BrowserDialog extends JDialog {
    private final StructuredTree<BrowserTreeStructure> tree;
    private Path path;

    public BrowserDialog() {
        super((JDialog) null, true);

        tree = new StructuredTree<>(new BrowserTreeStructure.Root());
        tree.setLabelProvider(new BrowserTreeLabelProvider());
        tree.setPlaceholderText("No games found.\n\nUse the Browse button to locate your game manually.");
        tree.setRootVisible(false);
        tree.setShowsRootHandles(false);
        tree.expand();

        var pane = new JScrollPane(tree);
        pane.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));

        var openButton = createButton("Open", KeyEvent.VK_O, this::openPressed);
        openButton.setEnabled(false);

        var browseButton = createButton("Browse", KeyEvent.VK_B, this::browsePressed);
        var exitButton = createButton("Exit", KeyEvent.VK_X, this::exitPressed);

        tree.addActionListener(new TreeActionListener() {
            @Override
            public void treePathClicked(TreeActionEvent event) {
                openPressed();
            }

            @Override
            public void treePathSelected(TreeActionEvent event) {
                openButton.setEnabled(event.getLastPathComponent() instanceof BrowserTreeStructure.Location);
            }
        });

        setLayout(new MigLayout("ins dialog,wrap", "fill,grow"));
        add(new JLabel("Detected locations:"));
        add(pane, "push,grow");
        add(browseButton, "split 3,wmin button,tag left");
        add(openButton, "wmin button,tag ok");
        add(exitButton, "wmin button,tag cancel");
        getRootPane().setDefaultButton(browseButton);
    }

    public Optional<Path> getPath() {
        return Optional.ofNullable(path);
    }

    private void browsePressed() {
        chooseGameDirectory().ifPresent(path -> {
            this.path = path;
            dispose();
        });
    }

    private void openPressed() {
        if (tree.getSelectionPathComponent() instanceof BrowserTreeStructure.Location(var path)) {
            this.path = path;
            dispose();
        }
    }

    private void exitPressed() {
        dispose();
    }

    private Optional<Path> chooseGameDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose game directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return Optional.of(chooser.getSelectedFile().toPath());
        } else {
            return Optional.empty();
        }
    }

    private static JButton createButton(String text, int mnemonic, Runnable runnable) {
        var button = new JButton(text);
        button.setMnemonic(mnemonic);
        button.addActionListener(_ -> runnable.run());
        return button;
    }
}
