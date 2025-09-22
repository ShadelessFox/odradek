package sh.adelessfox.odradek.app;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import sh.adelessfox.odradek.ui.components.view.View;
import sh.adelessfox.odradek.ui.components.view.ViewPanel;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;

public class Test {
    public static void main(String[] args) {
        FlatInspector.install("ctrl shift alt X");
        FlatUIDefaultsInspector.install("ctrl shift alt Y");
        FlatLightLaf.setup();

        var panel = new ViewPanel(ViewPanel.Placement.LEFT);
        panel.addPrimaryView("Explore streaming graph", Fugue.getIcon("blue-document"), new TextAreaView("Graph placeholder"));
        panel.addPrimaryView("Explore object links", Fugue.getIcon("chain--arrow"), new TextAreaView("Links placeholder"));
        panel.addSecondaryView("Import", Fugue.getIcon("folder-import"), new TextAreaView("Import"));
        panel.addSecondaryView("Export", Fugue.getIcon("folder-export"), new TextAreaView("Export"));
        panel.setContent(new JScrollPane(new JTextArea("Contents")));

        var frame = new JFrame();
        frame.setSize(640, 480);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.setVisible(true);
    }

    private static class TextAreaView implements View {
        private final String text;
        private JTextArea area;

        private TextAreaView(String text) {
            this.text = text;
        }

        @Override
        public JComponent createComponent() {
            area = new JTextArea(text);
            return new JScrollPane(area);
        }

        @Override
        public boolean isFocused() {
            return area.isFocusOwner();
        }

        @Override
        public void setFocus() {
            area.requestFocusInWindow();
        }
    }
}
