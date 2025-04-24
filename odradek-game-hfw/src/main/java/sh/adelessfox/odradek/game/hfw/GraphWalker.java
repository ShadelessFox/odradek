package sh.adelessfox.odradek.game.hfw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.storage.StreamingGraphResource;
import sh.adelessfox.odradek.game.hfw.storage.StreamingObjectReader;
import sh.adelessfox.odradek.rtti.runtime.TypedObject;

import java.io.IOException;
import java.io.UncheckedIOException;
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
        ForbiddenWestGame game,
        boolean readSubgroups
    ) throws IOException {
        return stream(type, game.getStreamingReader(), game.getStreamingGraph(), readSubgroups)::iterator;
    }

    public static <T extends TypedObject> Stream<SearchResult<T>> stream(
        Class<T> type,
        StreamingObjectReader reader,
        StreamingGraphResource graph,
        boolean readSubgroups
    ) throws IOException {
        return StreamSupport.stream(spliterator(type, reader, graph, readSubgroups), false);
    }

    private static <T extends TypedObject> Spliterator<SearchResult<T>> spliterator(
        Class<T> ofType,
        StreamingObjectReader reader,
        StreamingGraphResource graph,
        boolean readSubgroups
    ) throws IOException {
        return new Spliterator<>() {
            private StreamingObjectReader.GroupResult result;
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
                var groups = graph.groups();
                for (int i = nextGroupIndex; i < groups.size(); i++) {
                    var group = groups.get(i);
                    if (graph.types(group).stream().anyMatch(t -> t.isInstanceOf(ofType))) {
                        log.debug("Reading group {}/{}", i + 1, groups.size());
                        try {
                            result = reader.readGroup(group.groupID(), readSubgroups);
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
                if (result != null) {
                    var objects = result.objects();
                    for (int i = nextObjectIndex; i < objects.size(); i++) {
                        var object = objects.get(i).object();
                        if (ofType.isInstance(object)) {
                            action.accept(new SearchResult<>(result.group().groupID(), i, ofType.cast(object)));
                            nextObjectIndex = i + 1;
                            return true;
                        }
                    }
                }
                return false;
            }
        };
    }

    public record SearchResult<T>(int groupId, int objectIndex, T object) {
    }
}
