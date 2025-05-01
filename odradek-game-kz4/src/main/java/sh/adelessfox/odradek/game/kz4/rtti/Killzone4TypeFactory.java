package sh.adelessfox.odradek.game.kz4.rtti;

import sh.adelessfox.odradek.rtti.factory.AbstractTypeFactory;
import sh.adelessfox.odradek.rtti.factory.TypeId;
import sh.adelessfox.odradek.rtti.runtime.TypeInfo;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Killzone4TypeFactory extends AbstractTypeFactory {
    public Killzone4TypeFactory() {
        super(Killzone4.class, MethodHandles.lookup());
    }

    @Override
    protected TypeId computeTypeId(TypeInfo info) {
        return new Killzone4TypeId(info.name().fullName());
    }

    @Override
    protected void sortSerializableAttrs(List<OrderedAttr> attrs) {
        // This is a broken implementation of quicksort used by older versions of Decima
        quicksort(attrs, Comparator.comparingInt(OrderedAttr::offset), 0, attrs.size() - 1, 0);
    }

    @Override
    protected void filterSerializableAttrs(List<OrderedAttr> attrs) {
        // Remove save state attribute
        attrs.removeIf(attr -> (attr.info().flags() & 2) != 0);
        // Remove non-"serializable" attributes. They include holders for MsgReadBinary data
        attrs.removeIf(attr -> !attr.serializable());
    }

    private static <T> int quicksort(List<T> items, Comparator<T> comparator, int left, int right, int state) {
        if (left < right) {
            state = 0x19660D * state + 0x3C6EF35F;

            int pivot = (state >>> 8) % (right - left);
            Collections.swap(items, left + pivot, left);

            int start = partition(items, comparator, left, right);
            state = quicksort(items, comparator, left, start - 1, state);
            state = quicksort(items, comparator, start + 1, right, state);
        }

        return state;
    }

    private static <T> int partition(List<T> items, Comparator<T> comparator, int left, int right) {
        var l = left - 1;
        var r = right;

        while (true) {
            do {
                if (l >= r) {
                    break;
                }
                l++;
            } while (comparator.compare(items.get(l), items.get(right)) < 0);

            do {
                if (r <= l) {
                    break;
                }
                r--;
            } while (comparator.compare(items.get(right), items.get(r)) < 0);

            if (l >= r) {
                break;
            }

            Collections.swap(items, l, r);
        }

        Collections.swap(items, l, right);

        return l;
    }
}
