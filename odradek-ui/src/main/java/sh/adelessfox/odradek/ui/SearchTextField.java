package sh.adelessfox.odradek.ui;

import com.formdev.flatlaf.extras.components.FlatTextField;
import com.formdev.flatlaf.icons.FlatSearchIcon;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.time.Duration;

public final class SearchTextField extends FlatTextField {
    public static final Duration DEFAULT_DELAY = Duration.ofMillis(250);

    private final int delay;
    private Timer timer;

    public SearchTextField() {
        this(DEFAULT_DELAY);
    }

    public SearchTextField(Duration delay) {
        this.delay = Math.toIntExact(delay.toMillis());

        setShowClearButton(true);
        setLeadingIcon(new FlatSearchIcon(true));

        DocumentListener listener = (DocumentAdapter) _ -> schedule();
        getDocument().addDocumentListener(listener);
        addPropertyChangeListener("document", event -> {
            Document oldDocument = (Document) event.getOldValue();
            Document newDocument = (Document) event.getNewValue();

            if (oldDocument != null) {
                oldDocument.removeDocumentListener(listener);
            }

            if (newDocument != null) {
                newDocument.addDocumentListener(listener);
            }

            fireActionPerformed();
        });
        addActionListener(_ -> reset());
    }

    private void schedule() {
        reset();

        timer = new Timer(delay, _ -> fireActionPerformed());
        timer.setRepeats(false);
        timer.start();
    }

    private void reset() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }
}
