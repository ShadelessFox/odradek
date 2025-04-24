package sh.adelessfox.odradek.game.hfw.rtti;

import sh.adelessfox.odradek.hashing.HashFunction;
import sh.adelessfox.odradek.rtti.factory.AbstractTypeFactory;
import sh.adelessfox.odradek.rtti.factory.TypeId;
import sh.adelessfox.odradek.rtti.factory.TypeName;
import sh.adelessfox.odradek.rtti.runtime.TypeInfo;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HFWTypeFactory extends AbstractTypeFactory {
    public HFWTypeFactory() {
        super(HorizonForbiddenWest.class, MethodHandles.lookup());
    }

    @Override
    protected TypeId computeTypeId(TypeInfo info) {
        var name = "00000001_" + getInternalName(info.name());
        var hash = HashFunction.murmur3().hash(name).asLong();
        return HFWTypeId.of(hash);
    }

    @Override
    protected void sortSerializableAttrs(List<OrderedAttr> attrs) {
        quicksort(attrs, Comparator.comparingInt(OrderedAttr::offset), 0, attrs.size() - 1, 0);
    }

    @Override
    protected void filterSerializableAttrs(List<OrderedAttr> attrs) {
        // Remove save state attribute
        attrs.removeIf(attr -> (attr.info().flags() & 2) != 0);
        // Remove non-"serializable" attributes. They include holders for MsgReadBinary data
        attrs.removeIf(attr -> !attr.serializable());
    }

    private static String getInternalName(TypeName name) {
        return switch (name) {
            case TypeName.Simple(var n) -> n;
            case TypeName.Parameterized(var n, var a) -> n + '_' + getInternalName(a);
        };
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
