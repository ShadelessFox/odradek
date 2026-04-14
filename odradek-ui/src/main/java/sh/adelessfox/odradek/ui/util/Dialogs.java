package sh.adelessfox.odradek.ui.util;

import com.formdev.flatlaf.extras.components.FlatLabel;
import com.formdev.flatlaf.extras.components.FlatScrollPane;
import com.formdev.flatlaf.extras.components.FlatTextArea;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.PrintWriter;
import java.io.StringWriter;

public final class Dialogs {
    private static final String[] EXCEPTION_DIALOG_OPTIONS = {"Copy", "OK"};

    private Dialogs() {
    }

    public static void showExceptionDialog(Component parentComponent, String title, Throwable throwable) {
        var sw = new StringWriter();
        var pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);

        var caption = new FlatLabel();
        caption.setText(title);
        caption.setLabelType(FlatLabel.LabelType.large);

        var trace = new FlatTextArea();
        trace.setStyleClass("monospaced");
        trace.setEditable(false);
        trace.setText(sw.toString().replace("\t", "    "));
        trace.setCaretPosition(0);

        var pane = new FlatScrollPane();
        pane.setViewportView(trace);
        pane.setStyle("border: 1,1,1,1, $Component.borderColor");

        var panel = new JPanel(new MigLayout("ins 0,wrap"));
        panel.add(caption);
        panel.add(new JLabel("Stack trace:"));
        panel.add(pane, "w :600:,h :250:");

        int result = JOptionPane.showOptionDialog(
            parentComponent,
            panel,
            "An error occurred during program execution",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.ERROR_MESSAGE,
            null,
            EXCEPTION_DIALOG_OPTIONS,
            EXCEPTION_DIALOG_OPTIONS[0]);

        if (result == 0) {
            var contents = new StringSelection(sw.toString());
            var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(contents, contents);
        }
    }
}
