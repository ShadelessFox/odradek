package sh.adelessfox.odradek.game.decima;

import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.data.TypedObject;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public interface StreamingGraph {
    record Range<T>(int start, int count, List<T> inner) {
        public Range {
            Objects.checkFromIndexSize(start, count, inner.size());
        }

        public Stream<T> stream() {
            return inner.subList(start, start + count).stream();
        }

        public T get(int index) {
            Objects.checkIndex(index, count);
            return inner.get(start + index);
        }
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
    }

    /** A list of all object types in the streaming graph. */
    List<ClassTypeInfo> types();

    /** A list of all groups in the streaming graph. */
    List<? extends Group> groups();

    /** A list of all subgroups in the streaming graph. */
    List<? extends Group> subGroups();

    /** A list of all files referenced by the streaming graph. */
    List<String> files();

    /** Retrieves the group with the specified ID. */
    Group group(int id);

    LinkTable linkTable();

    /** Returns the game-specific resource object that represents this streaming graph. */
    TypedObject resource();
}
