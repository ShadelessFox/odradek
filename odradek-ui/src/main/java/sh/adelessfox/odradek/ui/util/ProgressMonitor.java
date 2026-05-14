package sh.adelessfox.odradek.ui.util;

import com.formdev.flatlaf.extras.components.FlatLabel;
import com.formdev.flatlaf.extras.components.FlatProgressBar;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Mostly a copy of {@link javax.swing.ProgressMonitor}, but prettier.
 */
public final class ProgressMonitor {
    private final Component parentComponent;
    private final String title;
    private final Object[] options;

    private JOptionPane pane;
    private JDialog dialog;
    private FlatLabel textLabel;
    private FlatProgressBar progressBar;
    private FlatLabel footerLabel;

    private String text;
    private int min;
    private int max;
    private int millisToDecideToPopup = 500;
    private int millisToPopup = 2000;

    private final long startTime = System.currentTimeMillis();

    public ProgressMonitor(Component parentComponent, String title, int min, int max) {
        this.parentComponent = parentComponent;
        this.title = title;
        this.options = new Object[]{UIManager.getString("OptionPane.cancelButtonText")};

        setMinimum(min);
        setMaximum(max);
    }

    public void setText(String text) {
        if (textLabel != null) {
            textLabel.setText(text);
        }
        this.text = text;
    }

    public void setMinimum(int minimum) {
        if (progressBar != null) {
            progressBar.setMinimum(minimum);
        }
        min = minimum;
    }

    public void setMaximum(int maximum) {
        if (progressBar != null) {
            progressBar.setMaximum(maximum);
        }
        max = maximum;
    }

    public void setProgress(int progress) {
        if (progress >= max) {
            close();
            return;
        }

        if (footerLabel != null) {
            footerLabel.setText("%d of %d".formatted(progress - min, max - min));
        }

        if (progressBar != null) {
            progressBar.setValue(progress);
            return;
        }

        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;
        if (elapsedTime < millisToDecideToPopup) {
            return;
        }

        long predictedCompletionTime;
        if (progress > min) {
            predictedCompletionTime = (elapsedTime * (max - min) / (progress - min));
        } else {
            predictedCompletionTime = millisToPopup;
        }

        if (predictedCompletionTime >= millisToPopup) {
            create();
            progressBar.setValue(progress);
        }
    }

    public void setMillisToDecideToPopup(int millisToDecideToPopup) {
        this.millisToDecideToPopup = millisToDecideToPopup;
    }

    public void setMillisToPopup(int millisToPopup) {
        this.millisToPopup = millisToPopup;
    }

    public boolean isCanceled() {
        if (pane == null) {
            return false;
        }
        Object value = pane.getValue();
        if (value == null) {
            return false;
        }
        return value == options[0] || value == Integer.valueOf(JOptionPane.CLOSED_OPTION);
    }

    public void close() {
        if (dialog != null) {
            dialog.setVisible(false);
            dialog.dispose();
            dialog = null;
        }
    }

    private void create() {
        progressBar = new FlatProgressBar();
        progressBar.setStringPainted(false);
        progressBar.setMinimum(min);
        progressBar.setMaximum(max);

        textLabel = new FlatLabel();
        textLabel.setLabelType(FlatLabel.LabelType.large);
        textLabel.setText(text);

        footerLabel = new FlatLabel();
        footerLabel.setText("...");

        var panel = new JPanel(new MigLayout("ins 0,wrap,w 400", "[grow,fill]"));
        panel.add(textLabel);
        panel.add(progressBar);
        panel.add(footerLabel);

        pane = new JOptionPane(
            panel,
            JOptionPane.PLAIN_MESSAGE,
            JOptionPane.DEFAULT_OPTION,
            null,
            options,
            options[0]
        );

        dialog = pane.createDialog(parentComponent, title);
        dialog.setModal(true);
        dialog.setVisible(true);
    }
}
