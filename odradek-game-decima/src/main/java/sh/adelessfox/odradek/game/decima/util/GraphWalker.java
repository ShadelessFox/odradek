package sh.adelessfox.odradek.game.decima.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.decima.DecimaGame;
import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.game.decima.StreamingGraph;
import sh.adelessfox.odradek.rtti.data.TypedObject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A utility class for traversing the graph's objects of a specific type.
 */
public final class GraphWalker {
    private static final Logger log = LoggerFactory.getLogger(GraphWalker.class);

    private GraphWalker() {
    }

    public static <T extends TypedObject> Iterable<SearchResult<T>> iterate(
        Class<T> type,
        DecimaGame game,
        boolean readSubgroups
    ) {
        return stream(type, game, readSubgroups)::iterator;
    }

    public static <T extends TypedObject> Stream<SearchResult<T>> stream(
        Class<T> type,
        DecimaGame game,
        boolean readSubgroups
    ) {
        return StreamSupport.stream(spliterator(type, game, readSubgroups), false);
    }

    private static <T extends TypedObject> Spliterator<SearchResult<T>> spliterator(
        Class<T> ofType,
        DecimaGame game,
        boolean readSubgroups
    ) {
        var groups = game.streamingGraph().groups().stream()
            .sorted(Comparator.comparingInt(StreamingGraph.Group::id))
            .toList();

        return new Spliterator<>() {
            private StreamingGraph.Group currentGroup;
            private List<TypedObject> currentObjects;
            private int nextGroupIndex;
            private int nextObjectIndex;

            @Override
            public boolean tryAdvance(Consumer<? super SearchResult<T>> action) {
                do {
                    if (tryAdvanceObject(action)) {
                        return true;
                    }
                } while (tryAdvanceGroup());
                return false;
            }

            @Override
            public Spliterator<SearchResult<T>> trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                return Long.MAX_VALUE;
            }

            @Override
            public int characteristics() {
                return ORDERED | NONNULL;
            }

            private boolean tryAdvanceGroup() {
                for (int i = nextGroupIndex; i < groups.size(); i++) {
                    var group = groups.get(i);
                    var matches = group.types().stream().anyMatch(t -> ofType.isAssignableFrom(t.type()));
                    if (matches) {
                        log.debug("[{}/{}] Reading group {}", i + 1, groups.size(), group.id());
                        try {
                            currentGroup = group;
                            currentObjects = game.readGroup(group.id(), readSubgroups);
                            nextGroupIndex = i + 1;
                            nextObjectIndex = 0;
                            return true;
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }
                }
                return false;
            }

            private boolean tryAdvanceObject(Consumer<? super SearchResult<T>> action) {
                if (currentGroup == null) {
                    return false;
                }
                for (int i = nextObjectIndex; i < currentObjects.size(); i++) {
                    var object = currentObjects.get(i);
                    if (ofType.isInstance(object)) {
                        action.accept(new SearchResult<>(new ObjectId(currentGroup.id(), i), ofType.cast(object)));
                        nextObjectIndex = i + 1;
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public record SearchResult<T>(ObjectId objectId, T object) {
    }
}
