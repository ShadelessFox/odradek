package sh.adelessfox.odradek.ui.editors.stack;

import sh.adelessfox.odradek.ui.editors.Editor;

import javax.swing.*;
import java.awt.*;

final class EditorComponent extends JComponent {
    final Editor editor;
    final Editor.Provider provider;

    public EditorComponent(JComponent component, Editor editor, Editor.Provider provider) {
        this.editor = editor;
        this.provider = provider;
        setLayout(new BorderLayout());
        setComponent(component);
    }

    EditorStack getEditorStack() {
        return (EditorStack) getParent();
    }

    boolean hasComponent() {
        BorderLayout layout = (BorderLayout) getLayout();
        return layout.getLayoutComponent(BorderLayout.CENTER) != null;
    }

    void setComponent(JComponent component) {
        setComponent(component, BorderLayout.CENTER);
    }

    private void setComponent(JComponent component, Object constraint) {
        var layout = (BorderLayout) getLayout();
        var currentComponent = layout.getLayoutComponent(constraint);
        boolean needsValidation = false;

        if (currentComponent != null) {
            remove(currentComponent);
            needsValidation = true;
        }

        if (component != null) {
            add(component, constraint);
            needsValidation = true;
        }

        if (needsValidation) {
            validate();
        }
    }
}
