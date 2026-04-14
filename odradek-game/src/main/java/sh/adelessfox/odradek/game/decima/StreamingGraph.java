package sh.adelessfox.odradek.game.decima;

import sh.adelessfox.odradek.rtti.ClassTypeInfo;

import java.util.List;
import java.util.stream.Stream;

public interface StreamingGraph {
    record Range<T>(int start, int count, List<T> inner) {
        public Range {
            if (start < 0) {
                throw new IllegalArgumentException("start must be non-negative");
            }
            if (count < 0) {
                throw new IllegalArgumentException("count must be non-negative");
            }
        }

        public Stream<T> stream() {
            return inner.subList(start, start + count).stream();
        }
    }

    interface Group {
        /** Unique identifier of the group. */
        int id();

        /** A range into {@link StreamingGraph#types()} that specifies the types of all objects in this group. */
        Range<ClassTypeInfo> types();
    }

    /** A list of all object types in the streaming graph. */
    List<ClassTypeInfo> types();

    /** A list of all groups in the streaming graph. */
    List<Group> groups();

    /** Retrieves the group with the specified ID. */
    Group group(int id);

    LinkTable linkTable();
}
