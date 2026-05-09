package sh.adelessfox.odradek.ui.components;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.awt.event.ActionEvent;

public final class SearchTextArea extends JPanel {
    private final JTextArea textArea = new JTextArea();
    private final SearchTextField searchField = new SearchTextField();

    public SearchTextArea() {
        var controller = new SearchController(textArea, searchField);

        searchField.setPlaceholderText("Search");
        searchField.setVisible(false);
        searchField.addActionListener(_ -> controller.findNext());
        searchField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("shift ENTER"), "find-previous");
        searchField.getActionMap().put("find-previous", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.findPrevious();
            }
        });

        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ctrl F"), "show-search");
        getActionMap().put("show-search", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchField.setVisible(true);
                searchField.requestFocusInWindow();
                searchField.selectAll();
                controller.findNext();
            }
        });

        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"), "hide-search");
        getActionMap().put("hide-search", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchField.setVisible(false);
                controller.clear();
                textArea.requestFocusInWindow();
            }
        });

        setLayout(new BorderLayout());
        add(new JScrollPane(textArea), BorderLayout.CENTER);
        add(searchField, BorderLayout.SOUTH);
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    public SearchTextField getSearchField() {
        return searchField;
    }

    private static final class SearchController {
        private static final DefaultHighlighter.DefaultHighlightPainter MATCH_PAINTER
            = new DefaultHighlighter.DefaultHighlightPainter(UIManager.getColor("TextSearchSupport.matchBackground"));
        private static final DefaultHighlighter.DefaultHighlightPainter CURRENT_MATCH_PAINTER
            = new DefaultHighlighter.DefaultHighlightPainter(UIManager.getColor("TextSearchSupport.currentMatchBackground"));

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
            field.setOutline(null);
            previousQuery = "";
        }

        private void find(boolean backwards) {
            var query = field.getText();
            area.getHighlighter().removeAllHighlights();

            if (query.isEmpty()) {
                field.setOutline(null);
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
                field.setOutline("error");
                previousQuery = query;
                return;
            }

            field.setOutline(null);
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
