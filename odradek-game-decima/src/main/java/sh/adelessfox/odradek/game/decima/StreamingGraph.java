package sh.adelessfox.odradek.game.decima;

import sh.adelessfox.odradek.hashing.HashCode;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.data.TypedObject;

import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;

public interface StreamingGraph {
    record Span(int fileIndex, int offset, int length) {
    }

    record Locator(int fileIndex, long offset) {
    }

    record Link(OptionalInt group, int index) {
    }

    interface Group {
        /** Unique identifier of the group. */
        int id();

        int typeStart();

        List<? extends Group> subGroups();

        List<? extends Group> superGroups();

        /** Indices of the root objects in this group. */
        List<Integer> roots();

        List<ClassTypeInfo> types();

        List<Span> spans();

        List<Locator> locators();

        Iterator<StreamingGraph.Link> links();
    }

    List<ClassTypeInfo> types();

    List<? extends Group> groups();

    List<String> files();

    /** Retrieves the group with the specified ID. */
    Group group(int id);

    Iterator<StreamingGraph.Link> links(int position);

    HashCode checksum();

    /** Returns the game-specific resource object that represents this streaming graph. */
    TypedObject resource();
}
