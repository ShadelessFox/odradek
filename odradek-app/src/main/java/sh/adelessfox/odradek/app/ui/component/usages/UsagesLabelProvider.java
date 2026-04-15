package sh.adelessfox.odradek.app.ui.component.usages;

import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;
import sh.adelessfox.odradek.ui.components.tree.StyledTreeLabelProvider;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;
import java.util.Optional;

final class UsagesLabelProvider implements StyledTreeLabelProvider<UsagesStructure> {
    private final ForbiddenWestGame game;

    UsagesLabelProvider(ForbiddenWestGame game) {
        this.game = game;
    }

    @Override
    public Optional<StyledText> getStyledText(UsagesStructure element) {
        return switch (element) {
            case UsagesStructure.Root _ -> Optional.empty();
            case UsagesStructure.Objects _ -> StyledText.builder()
                .add("Object", StyledFragment.BOLD)
                .build();
            case UsagesStructure.Object(var object) -> StyledText.builder()
                .add("[" + object.toString() + "]")
                .add(" " + getType(object.groupId(), object.objectIndex()), StyledFragment.NAME)
                .build();
            case UsagesStructure.Links(var type, var links) -> StyledText.builder()
                .add(getText(type), StyledFragment.BOLD)
                .add("  " + (links.size() == 1 ? "1 result" : links.size() + " results"), StyledFragment.GRAYED)
                .build();
            case UsagesStructure.Link(_, var link) -> StyledText.builder()
                .add("[" + link.groupId() + ":" + link.objectIndex() + "]")
                .add(" " + getType(link.groupId(), link.objectIndex()), StyledFragment.NAME)
                .add(" " + link.path(), StyledFragment.GRAYED)
                .build();
        };
    }

    @Override
    public Optional<Icon> getIcon(UsagesStructure element) {
        return switch (element) {
            case UsagesStructure.Object _ -> Optional.of(Fugue.getIcon("blue-document"));
            case UsagesStructure.Link(var type, _) -> Optional.of(getIcon(type));
            default -> Optional.empty();
        };
    }

    private static String getText(UsagesStructure.Type type) {
        return switch (type) {
            case INCOMING -> "Incoming links";
            case OUTGOING -> "Outgoing links";
        };
    }

    private static Icon getIcon(UsagesStructure.Type type) {
        return switch (type) {
            case INCOMING -> Fugue.getIcon("blue-document-import");
            case OUTGOING -> Fugue.getIcon("blue-document-export");
        };
    }

    private ClassTypeInfo getType(int groupId, int objectIndex) {
        var graph = game.streamingGraph();
        var group = graph.group(groupId);
        return group.types().get(objectIndex);
    }
}
