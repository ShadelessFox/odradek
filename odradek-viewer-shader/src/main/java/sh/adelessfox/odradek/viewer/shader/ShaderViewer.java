package sh.adelessfox.odradek.viewer.shader;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.graphics.Program;
import sh.adelessfox.odradek.graphics.ProgramType;
import sh.adelessfox.odradek.graphics.Shader;
import sh.adelessfox.odradek.ui.Viewer;
import sh.adelessfox.odradek.ui.components.SearchTextField;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;

public record ShaderViewer(Shader shader, Game game) implements Viewer {
    public static final class Provider implements Viewer.Provider<Shader> {
        @Override
        public Viewer create(Shader object, Game game) {
            return new ShaderViewer(object, game);
        }

        @Override
        public String name() {
            return "Shader";
        }

        @Override
        public Optional<String> icon() {
            return Optional.of("fugue:color");
        }
    }

    @Override
    public JComponent createComponent() {
        var disassembled = shader.programs().stream()
            .map(Program::disassemble)
            .toList();

        return createShaderPanel(shader, disassembled);
    }

    private static JComponent createShaderPanel(Shader shader, List<String> disassembled) {
        var pane = new JTabbedPane();
        for (int i = 0; i < shader.programs().size(); i++) {
            var program = shader.programs().get(i);
            var text = disassembled.get(i);
            pane.add(toDisplayString(program.type()), createProgramPanel(text));
        }

        return pane;
    }

    private static JComponent createProgramPanel(String text) {
        var area = new JTextArea();
        area.setFont(UIManager.getFont("monospaced.font"));
        area.setEditable(false);
        area.setText(text);
        area.setCaretPosition(0);

        var searchField = new SearchTextField();
        var controller = new SearchController(area, searchField);

        searchField.setPlaceholderText("Find");
        searchField.setVisible(false);
        searchField.addActionListener(_ -> controller.findNext());
        searchField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ESCAPE"), "hide-search");
        searchField.getActionMap().put("hide-search", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchField.setVisible(false);
                controller.clear();
                area.requestFocusInWindow();
            }
        });
        searchField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("shift ENTER"), "find-previous");
        searchField.getActionMap().put("find-previous", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.findPrevious();
            }
        });

        var panel = new JPanel(new BorderLayout());
        panel.add(searchField, BorderLayout.NORTH);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ctrl F"), "show-search");
        panel.getActionMap().put("show-search", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchField.setVisible(true);
                searchField.requestFocusInWindow();
                searchField.selectAll();
                controller.findNext();
            }
        });

        return panel;
    }

    private static String toDisplayString(ProgramType type) {
        return switch (type) {
            case VERTEX_PROGRAM -> "Vertex Program";
            case PIXEL_PROGRAM -> "Pixel Program";
            case COMPUTE_PROGRAM -> "Compute Program";
            case GEOMETRY_PROGRAM -> "Geometry Program";
        };
    }

    private static final class SearchController {
        private static final DefaultHighlighter.DefaultHighlightPainter MATCH_PAINTER =
            new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 235, 156));
        private static final DefaultHighlighter.DefaultHighlightPainter CURRENT_MATCH_PAINTER =
            new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 193, 7));

        private final JTextArea area;
        private final SearchTextField field;
        private String previousQuery = "";

        private SearchController(JTextArea area, SearchTextField field) {
            this.area = area;
            this.field = field;
        }

        private void findNext() {
            find(false);
        }

        private void findPrevious() {
            find(true);
        }

        private void clear() {
            area.getHighlighter().removeAllHighlights();
            field.putClientProperty("JComponent.outline", null);
            previousQuery = "";
        }

        private void find(boolean backwards) {
            var query = field.getText();
            area.getHighlighter().removeAllHighlights();

            if (query.isEmpty()) {
                field.putClientProperty("JComponent.outline", null);
                previousQuery = query;
                return;
            }

            var text = area.getText();
            var queryChanged = !query.equals(previousQuery);
            var start = queryChanged ? 0 : nextStart(backwards);
            var index = backwards && !queryChanged
                ? findPrevious(text, query, start)
                : findNext(text, query, start);

            if (index < 0) {
                field.putClientProperty("JComponent.outline", "error");
                previousQuery = query;
                return;
            }

            field.putClientProperty("JComponent.outline", null);
            highlightMatches(text, query, index);
            selectMatch(index, index + query.length());
            previousQuery = query;
        }

        private int nextStart(boolean backwards) {
            return backwards
                ? Math.max(0, area.getSelectionStart() - 1)
                : area.getSelectionEnd();
        }

        private static int findNext(String text, String query, int start) {
            var index = text.indexOf(query, start);
            return index >= 0 ? index : text.indexOf(query);
        }

        private static int findPrevious(String text, String query, int start) {
            var index = text.lastIndexOf(query, start);
            return index >= 0 ? index : text.lastIndexOf(query);
        }

        private void highlightMatches(String text, String query, int currentIndex) {
            var highlighter = area.getHighlighter();
            var index = text.indexOf(query);

            while (index >= 0) {
                try {
                    highlighter.addHighlight(
                        index,
                        index + query.length(),
                        index == currentIndex ? CURRENT_MATCH_PAINTER : MATCH_PAINTER
                    );
                } catch (BadLocationException e) {
                    throw new IllegalStateException("Unable to highlight search result", e);
                }

                index = text.indexOf(query, index + query.length());
            }
        }

        private void selectMatch(int start, int end) {
            area.setCaretPosition(start);
            area.moveCaretPosition(end);

            try {
                var bounds = area.modelToView2D(start).getBounds();
                area.scrollRectToVisible(bounds);
            } catch (BadLocationException e) {
                throw new IllegalStateException("Unable to scroll to search result", e);
            }
        }
    }
}
