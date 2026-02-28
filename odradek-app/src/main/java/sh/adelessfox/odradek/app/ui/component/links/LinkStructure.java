package sh.adelessfox.odradek.app.ui.component.links;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.tools.refs.LinkProvider;
import sh.adelessfox.odradek.game.ObjectId;
import sh.adelessfox.odradek.game.ObjectIdHolder;
import sh.adelessfox.odradek.ui.components.tree.TreeStructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public sealed interface LinkStructure extends TreeStructure<LinkStructure> {
    Logger log = LoggerFactory.getLogger(LinkStructure.class);

    @Override
    default List<? extends LinkStructure> getChildren() {
        return switch (this) {
            case Root(var provider, var object) -> {
                var nodes = new ArrayList<LinkStructure>();
                nodes.add(new Objects(object));
                try {
                    nodes.add(new Links(Type.INCOMING, provider.getIncomingLinks(object)));
                } catch (IOException e) {
                    log.error("Failed to get incoming links for object {}", object, e);
                }
                try {
                    nodes.add(new Links(Type.OUTGOING, provider.getOutgoingLinks(object)));
                } catch (IOException e) {
                    log.error("Failed to get outgoing links for object {}", object, e);
                }
                yield nodes.stream()
                    .filter(n -> !(n instanceof Links links && links.links().isEmpty()))
                    .toList();
            }
            case Objects(var object) -> List.of(new Object(object));
            case Object _ -> List.of();
            case Links(var type, var links) -> links.stream()
                .map(link -> new Link(type, link))
                .toList();
            case Link _ -> List.of();
        };
    }

    @Override
    default boolean hasChildren() {
        return switch (this) {
            case Root _, Objects _ -> true;
            case Links(_, var links) -> !links.isEmpty();
            case Link _, Object _ -> false;
        };
    }

    enum Type {
        INCOMING,
        OUTGOING
    }

    record Root(LinkProvider provider, ObjectId object) implements LinkStructure {
    }

    record Objects(ObjectId object) implements LinkStructure {
    }

    record Object(ObjectId objectId) implements LinkStructure, ObjectIdHolder {
    }

    record Links(Type type, List<LinkProvider.Link> links) implements LinkStructure {
    }

    record Link(Type type, LinkProvider.Link link) implements LinkStructure, ObjectIdHolder {
        @Override
        public ObjectId objectId() {
            return new ObjectId(link.groupId(), link.objectIndex());
        }
    }
}
