package sh.adelessfox.odradek.app.ui.component.links;

import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;
import sh.adelessfox.odradek.ui.components.tree.StyledTreeLabelProvider;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;
import java.util.Optional;

final class LinkLabelProvider implements StyledTreeLabelProvider<LinkStructure> {
    private final ForbiddenWestGame game;

    LinkLabelProvider(ForbiddenWestGame game) {
        this.game = game;
    }

    @Override
    public Optional<StyledText> getStyledText(LinkStructure element) {
        return switch (element) {
            case LinkStructure.Root _ -> Optional.empty();
            case LinkStructure.Objects _ -> StyledText.builder()
                .add("Object", StyledFragment.BOLD)
                .build();
            case LinkStructure.Object(var object) -> StyledText.builder()
                .add("[" + object.toString() + "]")
                .add(" " + getType(object.groupId(), object.objectIndex()), StyledFragment.NAME)
                .build();
            case LinkStructure.Links(var type, var links) -> StyledText.builder()
                .add(type == LinkStructure.Links.Type.INCOMING ? "Incoming links" : "Outgoing links",
                    StyledFragment.BOLD)
                .add("  " + links.size() + " results", StyledFragment.GRAYED)
                .build();
            case LinkStructure.Link(_, var link) -> StyledText.builder()
                .add("[" + link.groupId() + ":" + link.objectIndex() + "]")
                .add(" " + getType(link.groupId(), link.objectIndex()), StyledFragment.NAME)
                .add(" " + link.path(), StyledFragment.GRAYED)
                .build();
        };
    }

    @Override
    public Optional<Icon> getIcon(LinkStructure element) {
        return switch (element) {
            case LinkStructure.Object _ -> Optional.of(Fugue.getIcon("blue-document"));
            case LinkStructure.Link(var type, _) -> Optional.of(Fugue.getIcon(type == LinkStructure.Type.INCOMING
                ? "blue-document-import"
                : "blue-document-export"));
            default -> Optional.empty();
        };
    }

    private ClassTypeInfo getType(int groupId, int objectIndex) {
        var graph = game.getStreamingGraph();
        var group = graph.group(groupId);
        return graph.types().get(group.typeStart() + objectIndex);
    }
}
