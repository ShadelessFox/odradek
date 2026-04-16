package sh.adelessfox.odradek.app.ui.component.usages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.game.decima.ObjectIdHolder;
import sh.adelessfox.odradek.ui.components.tree.TreeStructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

sealed interface UsagesStructure extends TreeStructure<UsagesStructure> {
    Logger log = LoggerFactory.getLogger(UsagesStructure.class);

    @Override
    default List<? extends UsagesStructure> getChildren() {
        return switch (this) {
            case Root(var provider, var object) -> {
                var nodes = new ArrayList<UsagesStructure>();
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

    record Root(LinkDatabase provider, ObjectId object) implements UsagesStructure {
    }

    record Objects(ObjectId object) implements UsagesStructure {
    }

    record Object(ObjectId objectId) implements UsagesStructure, ObjectIdHolder {
    }

    record Links(Type type, List<LinkDatabase.Link> links) implements UsagesStructure {
        @Override
        public boolean equals(java.lang.Object obj) {
            return obj instanceof Links(var type1, _) && type == type1;
        }

        @Override
        public int hashCode() {
            return type.hashCode();
        }
    }

    record Link(Type type, LinkDatabase.Link link) implements UsagesStructure, ObjectIdHolder {
        @Override
        public ObjectId objectId() {
            return new ObjectId(link.groupId(), link.objectIndex());
        }

        @Override
        public boolean equals(java.lang.Object obj) {
            return obj instanceof Link(Type type1, LinkDatabase.Link link1) && type == type1 && link.equals(link1);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(type, link);
        }
    }
}
