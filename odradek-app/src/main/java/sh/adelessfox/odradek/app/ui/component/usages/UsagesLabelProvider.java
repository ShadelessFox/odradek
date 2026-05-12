package sh.adelessfox.odradek.app.ui.component.usages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.decima.ContainerTypeRegistry;
import sh.adelessfox.odradek.game.decima.DecimaGame;
import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;
import sh.adelessfox.odradek.ui.components.tree.StyledTreeLabelProvider;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;
import java.util.Optional;

final class UsagesLabelProvider implements StyledTreeLabelProvider<UsagesStructure> {
    private static final Logger log = LoggerFactory.getLogger(UsagesLabelProvider.class);

    private final DecimaGame game;
    private final ContainerTypeRegistry registry;

    UsagesLabelProvider(DecimaGame game) {
        this.game = game;
        this.registry = ContainerTypeRegistry.lookup(game).orElse(null);
    }

    @Override
    public Optional<StyledText> getStyledText(UsagesStructure element) {
        return switch (element) {
            case UsagesStructure.Root _, UsagesStructure.ContainersRoot _ -> Optional.empty();
            case UsagesStructure.Objects _ -> StyledText.builder()
                .add("Object", StyledFragment.BOLD)
                .build();
            case UsagesStructure.Object(var object) -> objectLabel(object);
            case UsagesStructure.Links(var type, var links) -> StyledText.builder()
                .add(getText(type), StyledFragment.BOLD)
                .add("  " + (links.size() == 1 ? "1 result" : links.size() + " results"), StyledFragment.GRAYED)
                .build();
            case UsagesStructure.Link(_, var link) -> StyledText.builder()
                .add("[" + link.groupId() + ":" + link.objectIndex() + "]")
                .add(" " + getType(link.groupId(), link.objectIndex()), StyledFragment.NAME)
                .add(" " + link.path(), StyledFragment.GRAYED)
                .build();
            case UsagesStructure.Containers(var containers) -> StyledText.builder()
                .add("Containing models", StyledFragment.BOLD)
                .add("  " + (containers.size() == 1 ? "1 result" : containers.size() + " results"), StyledFragment.GRAYED)
                .build();
            case UsagesStructure.Container(var object) -> objectLabel(object);
        };
    }

    @Override
    public Optional<Icon> getIcon(UsagesStructure element) {
        return switch (element) {
            case UsagesStructure.Object _, UsagesStructure.Container _ -> Optional.of(Fugue.getIcon("blue-document"));
            case UsagesStructure.Link(var type, _) -> Optional.of(getIcon(type));
            case UsagesStructure.Containers _ -> Optional.of(Fugue.getIcon("target"));
            default -> Optional.empty();
        };
    }

    private Optional<StyledText> objectLabel(ObjectId object) {
        var builder = StyledText.builder()
            .add("[" + object + "]")
            .add(" " + getType(object.groupId(), object.objectIndex()).name(), StyledFragment.NAME);
        resolveName(object).ifPresent(name -> builder.add(" " + name, StyledFragment.GRAYED));
        return builder.build();
    }

    private Optional<String> resolveName(ObjectId object) {
        if (registry == null) {
            return Optional.empty();
        }
        try {
            return registry.nameOf(game.readObject(object));
        } catch (Exception e) {
            log.debug("Failed to resolve name for {}", object, e);
            return Optional.empty();
        }
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
