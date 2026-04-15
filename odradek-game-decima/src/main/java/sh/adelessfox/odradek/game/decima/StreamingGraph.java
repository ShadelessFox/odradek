package sh.adelessfox.odradek.game.decima;

import sh.adelessfox.odradek.hashing.HashCode;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.data.TypedObject;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.Stream;

public interface StreamingGraph {
    record Range<T>(int start, int count, List<T> inner) implements Iterable<T> {
        public Range {
            Objects.checkFromIndexSize(start, count, inner.size());
        }

        public Stream<T> stream() {
            return slice().stream();
        }

        public T get(int index) {
            Objects.checkIndex(index, count);
            return inner.get(start + index);
        }

        @Override
        public Iterator<T> iterator() {
            return slice().iterator();
        }

        private List<T> slice() {
            return inner.subList(start, start + count);
        }
    }

    record Span(int fileIndex, int offset, int length) {
    }

    record Locator(int fileIndex, long offset) {
    }

    record Link(OptionalInt group, int index) {
    }

    interface Group {
        /** Unique identifier of the group. */
        int id();

        /** A range into {@link StreamingGraph#subGroups()} that specifies the subgroups of this group. */
        Range<? extends Group> subGroups();

        /** Indices of the root objects in this group. */
        List<Integer> roots();

        /** A range into {@link StreamingGraph#types()} that specifies the types of all objects in this group. */
        Range<ClassTypeInfo> types();

        /** A range into {@link StreamingGraph#spans()} that specifies the source data spans for this group. */
        Range<Span> spans();

        /** A range into {@link StreamingGraph#locators()} that specifies the locators for this group. */
        Range<Locator> locators();

        Iterator<StreamingGraph.Link> links();
    }

    List<ClassTypeInfo> types();

    List<? extends Group> groups();

    List<? extends Group> subGroups();

    List<Span> spans();

    List<Locator> locators();

    List<String> files();

    Iterator<StreamingGraph.Link> links(int position);

    HashCode checksum();

    /** Retrieves the group with the specified ID. */
    Group group(int id);

    /** Returns the game-specific resource object that represents this streaming graph. */
    TypedObject resource();
}
