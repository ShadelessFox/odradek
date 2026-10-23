package sh.adelessfox.odradek.app.ui.component.onboarding;

import com.formdev.flatlaf.util.UIScale;
import net.miginfocom.swing.MigLayout;
import sh.adelessfox.odradek.app.ui.settings.ApplicationSettings;
import sh.adelessfox.odradek.settings.Settings;
import sh.adelessfox.odradek.ui.components.LineBorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

final class OnboardingPanel extends JPanel {
    private final List<OnboardingPage> pages = OnboardingPage.load();
    private final JList<OnboardingPage> sections = new JList<>(pages.toArray(OnboardingPage[]::new));
    private final JEditorPane content = new JEditorPane();
    private final JLabel heading = new JLabel();
    private final JButton back = new JButton("Back");
    private final JButton next = new JButton("Next");

    OnboardingPanel(Settings settings, Runnable close) {
        super(new MigLayout("ins 16,fill", "[150:pref][grow,fill]", "[grow,fill][]"));

        sections.setName("sections");
        sections.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
            ) {
                var page = (OnboardingPage) value;
                return super.getListCellRendererComponent(list, (index + 1) + ". " + page.label(), index, isSelected, cellHasFocus);
            }
        });
        sections.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sections.setFixedCellHeight(UIScale.scale(36));

        var sectionsPane = new JScrollPane(sections);
        sectionsPane.setBorder(LineBorder.of());
        add(sectionsPane, "grow");

        heading.putClientProperty("FlatLaf.styleClass", "h1");
        content.setName("content");
        content.setContentType("text/html");
        content.setEditable(false);
        content.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        content.setFont(UIManager.getFont("Label.font"));
        content.setMargin(new Insets(8, 8, 8, 8));

        var page = new JPanel(new BorderLayout(0, UIScale.scale(8)));
        page.add(heading, BorderLayout.NORTH);
        page.add(new JScrollPane(content));
        add(page, "grow,wrap");

        var showOnStartup = new JCheckBox("Show on startup", settings.get(ApplicationSettings.SHOW_GETTING_STARTED_ON_STARTUP).value());
        showOnStartup.setName("showOnStartup");
        showOnStartup.setMnemonic(KeyEvent.VK_S);
        showOnStartup.setToolTipText("Otherwise, the guide opens automatically only the first time. Reopen it from Help > Getting Started.");
        showOnStartup.addActionListener(_ -> settings.get(ApplicationSettings.SHOW_GETTING_STARTED_ON_STARTUP).set(showOnStartup.isSelected()));

        back.setName("back");
        back.setMnemonic(KeyEvent.VK_B);
        back.addActionListener(_ -> sections.setSelectedIndex(sections.getSelectedIndex() - 1));
        next.setName("next");
        next.addActionListener(_ -> {
            if (sections.getSelectedIndex() == pages.size() - 1) {
                close.run();
            } else {
                sections.setSelectedIndex(sections.getSelectedIndex() + 1);
            }
        });

        var footer = new JPanel(new MigLayout("ins 0,fillx", "[][grow]10[][]", "[]"));
        footer.add(showOnStartup);
        footer.add(new JLabel("Reopen using Help > Getting Started"));
        footer.add(back, "sg navigation");
        footer.add(next, "sg navigation");
        add(footer, "span 2,growx,gaptop 12");

        sections.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && sections.getSelectedValue() != null) {
                showPage(sections.getSelectedValue());
            }
        });
        sections.setSelectedIndex(0);
    }

    JButton defaultButton() {
        return next;
    }

    private void showPage(OnboardingPage page) {
        heading.setText(page.title());
        content.setText("<html><body>" + String.join("\n", page.body()) + "</body></html>");
        content.setCaretPosition(0);

        int index = sections.getSelectedIndex();
        back.setEnabled(index > 0);
        var last = index == pages.size() - 1;
        next.setText(last ? "Done" : "Next");
        next.setMnemonic(last ? KeyEvent.VK_D : KeyEvent.VK_N);
    }
}
