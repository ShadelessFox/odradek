package sh.adelessfox.odradek.ui.util;

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

    public static void showExceptionDialog(Component parentComponent, String message, Throwable throwable) {
        var sw = new StringWriter();
        var pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);

        var caption = new FlatTextArea();
        caption.setStyle("font: $large.font; background: $Label.background; margin: 0,0,0,0");
        caption.setText(message);
        caption.setLineWrap(true);
        caption.setWrapStyleWord(true);
        caption.setEditable(false);
        caption.setCaretPosition(0);

        var trace = new FlatTextArea();
        trace.setStyleClass("monospaced");
        trace.setText(sw.toString().replace("\t", "    "));
        trace.setEditable(false);
        trace.setCaretPosition(0);

        var tracePane = new FlatScrollPane();
        tracePane.setViewportView(trace);
        tracePane.setStyle("border: 1,1,1,1, $Component.borderColor");

        var panel = new JPanel(new MigLayout("ins 0,wrap,w 600", "[grow,fill]"));
        panel.add(new JScrollPane(caption), "h pref::");
        panel.add(new JLabel("Stack trace:"));
        panel.add(tracePane, "h :250:");

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
