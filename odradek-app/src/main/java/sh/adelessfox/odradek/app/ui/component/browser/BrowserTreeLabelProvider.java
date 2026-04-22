package sh.adelessfox.odradek.app.ui.component.browser;

import sh.adelessfox.odradek.ui.components.tree.TreeLabelProvider;

import java.util.Optional;

final class BrowserTreeLabelProvider implements TreeLabelProvider<BrowserTreeStructure> {
    @Override
    public Optional<String> getText(BrowserTreeStructure element) {
        return switch (element) {
            case BrowserTreeStructure.Root _ -> Optional.empty();
            case BrowserTreeStructure.Location(var path) -> Optional.of(path.toString());
        };
    }
}
