package sh.adelessfox.odradek.app.ui.menu.main.help;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.BuildInfo;
import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.menu.main.MainMenu;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

@ActionRegistration(text = "&About")
@ActionContribution(parent = MainMenu.Help.ID)
public class AboutAction extends Action {
    private static final Logger log = LoggerFactory.getLogger(AboutAction.class);

    @Override
    public void perform(ActionContext context) {
        JOptionPane.showMessageDialog(
            JOptionPane.getRootFrame(),
            createContent(AboutAction::openLink),
            "About",
            JOptionPane.PLAIN_MESSAGE,
            new FlatSVGIcon(Application.class.getResource("application.svg")).derive(64, 64));
    }

    static JEditorPane createContent(Consumer<URI> openLink) {
        var upstream = link(BuildInfo.upstreamUrl(), BuildInfo.upstreamUrl().toString());
        var commit = BuildInfo.commitHash()
            .map(hash -> link(URI.create(BuildInfo.upstreamUrl() + "/commit/" + hash), hash))
            .orElse("unknown");

        var content = new JEditorPane();
        content.setContentType("text/html");
        content.setEditable(false);
        content.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        content.setFont(UIManager.getFont("Label.font"));
        content.setBorder(BorderFactory.createEmptyBorder());
        content.setText("""
            <html><body>
            <h2>Odradek %s</h2>
            <p>An asset viewer/extractor for Horizon Forbidden<br>West and Death Stranding 2.</p>
            <p>Upstream: %s<br>Commit: %s<br>Built on %s (UTC)</p>
            <p>© 2025-2026 ShadelessFox and contributors</p>
            </body></html>
            """.formatted(
            escapeHtml(BuildInfo.version()),
            upstream,
            commit,
            BuildInfo.buildDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        content.setCaretPosition(0);
        content.addHyperlinkListener(event -> {
            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                openLink.accept(URI.create(event.getDescription()));
            }
        });
        return content;
    }

    private static String link(URI uri, String text) {
        return "<a href=\"%s\">%s</a>".formatted(escapeHtml(uri.toString()), escapeHtml(text));
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }

    private static void openLink(URI uri) {
        try {
            Desktop.getDesktop().browse(uri);
        } catch (IOException | UnsupportedOperationException | SecurityException e) {
            log.error("Unable to open {}", uri, e);
            JOptionPane.showMessageDialog(
                JOptionPane.getRootFrame(),
                "Unable to open the browser. Open this link manually:\n" + uri,
                "Unable to open link",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
