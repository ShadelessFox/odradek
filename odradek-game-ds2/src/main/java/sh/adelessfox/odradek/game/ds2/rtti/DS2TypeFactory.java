package sh.adelessfox.odradek.game.ds2.rtti;

import sh.adelessfox.odradek.hashing.HashFunction;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.rtti.factory.AbstractTypeFactory;
import sh.adelessfox.odradek.rtti.factory.TypeId;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class DS2TypeFactory extends AbstractTypeFactory {
    public DS2TypeFactory() {
        super(DS2.class, MethodHandles.lookup());
    }

    @Override
    protected TypeId computeTypeId(TypeInfo info) {
        var name = "00000001_" + info.name();
        var hash = HashFunction.murmur3().hash(name).asLong();
        return DS2TypeId.of(hash);
    }

    @Override
    protected void sortOrderedAttributes(List<OrderedAttr> attrs) {
        quicksort(attrs, (o1, o2) -> {
            if (o1.attr().isProperty()) {
                if (!o2.attr().isProperty()) {
                    return 1;
                }
                return o1.attr().name().compareTo(o2.attr().name());
            } else {
                if (o2.attr().isProperty()) {
                    return -1;
                }
                return Integer.compare(o1.offset(), o2.offset());
            }
        });
    }

    @Override
    protected void filterOrderedAttributes(List<OrderedAttr> attrs) {
        attrs.removeIf(attr -> !attr.attr().isSerialized());
    }

    private static <T> void quicksort(List<T> items, Comparator<T> comparator) {
        quicksort(items, comparator, 0, items.size() - 1, 0);
    }

    private static <T> int quicksort(List<T> items, Comparator<T> comparator, int left, int right, int state) {
        if (left < right) {
            state = 0x19660D * state + 0x3C6EF35F;

            final int pivot = (state >>> 8) % (right - left);
            Collections.swap(items, left + pivot, right);

            final int start = partition(items, comparator, left, right);
            state = quicksort(items, comparator, left, start - 1, state);
            state = quicksort(items, comparator, start + 1, right, state);
        }

        return state;
    }

    private static <T> int partition(List<T> items, Comparator<T> comparator, int left, int right) {
        int start = left - 1;
        int end = right;

        while (true) {
            do {
                start++;
            } while (start < end && comparator.compare(items.get(start), items.get(right)) < 0);

            do {
                end--;
            } while (end > start && comparator.compare(items.get(right), items.get(end)) < 0);

            if (start >= end) {
                break;
            }

            Collections.swap(items, start, end);
        }

        Collections.swap(items, start, right);

        return start;
    }
}
