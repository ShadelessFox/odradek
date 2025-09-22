package sh.adelessfox.odradek.app;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import sh.adelessfox.odradek.ui.components.view.ViewPanel;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;

public class Test {
    public static void main(String[] args) {
        FlatInspector.install("ctrl shift alt X");
        FlatUIDefaultsInspector.install("ctrl shift alt Y");
        FlatLightLaf.setup();

        var panel = new ViewPanel(ViewPanel.Placement.LEFT);
        panel.addPrimaryView("Explore streaming graph", Fugue.getIcon("blue-document"), () -> new JLabel("Graph placeholder", SwingConstants.CENTER));
        panel.addPrimaryView("Explore object links", Fugue.getIcon("chain--arrow"), () -> new JLabel("Links placeholder", SwingConstants.CENTER));
        panel.addSecondaryView("Import", Fugue.getIcon("folder-import"), () -> new JLabel("Import", SwingConstants.CENTER));
        panel.addSecondaryView("Export", Fugue.getIcon("folder-export"), () -> new JLabel("Export", SwingConstants.CENTER));
        panel.setContent(new JLabel("Contents", SwingConstants.CENTER));

        var frame = new JFrame();
        frame.setSize(640, 480);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.setVisible(true);
    }
}
