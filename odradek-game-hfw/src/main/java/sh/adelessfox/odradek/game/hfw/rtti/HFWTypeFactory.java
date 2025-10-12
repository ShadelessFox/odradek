package sh.adelessfox.odradek.game.hfw.rtti;

import sh.adelessfox.odradek.hashing.HashFunction;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.rtti.factory.AbstractTypeFactory;
import sh.adelessfox.odradek.rtti.factory.TypeId;

import java.lang.invoke.MethodHandles;
import java.math.BigInteger;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class HFWTypeFactory extends AbstractTypeFactory {
    public HFWTypeFactory() {
        super(HorizonForbiddenWest.class, MethodHandles.lookup());
    }

    @Override
    protected TypeId computeTypeId(TypeInfo info) {
        var name = "00000001_" + info.name();
        var hash = HashFunction.murmur3().hash(name).asLong();
        return HFWTypeId.of(hash);
    }

    @Override
    protected void sortOrderedAttributes(List<OrderedAttr> attrs) {
        quicksort(attrs, Comparator.comparingInt(OrderedAttr::offset), 0, attrs.size() - 1, 0);
    }

    @Override
    protected void filterOrderedAttributes(List<OrderedAttr> attrs) {
        // Remove save-state attribute
        attrs.removeIf(attr -> !attr.attr().isSerialized());
    }

    @Override
    protected URL getTypes() {
        return getClass().getResource("/types.json");
    }

    @Override
    protected URL getExtensions() {
        return getClass().getResource("/extensions.json");
    }

    @Override
    protected Map<String, Class<?>> getBuiltins() {
        return Map.ofEntries(
            Map.entry("HalfFloat", float.class),
            Map.entry("float", float.class),
            Map.entry("double", double.class),
            Map.entry("bool", boolean.class),
            Map.entry("wchar", char.class),
            Map.entry("ucs4", int.class),
            Map.entry("uint", int.class),
            Map.entry("uint8", byte.class),
            Map.entry("uint16", short.class),
            Map.entry("uint32", int.class),
            Map.entry("uint64", long.class),
            Map.entry("uint128", BigInteger.class),
            Map.entry("uintptr", long.class),
            Map.entry("int", int.class),
            Map.entry("int8", byte.class),
            Map.entry("int16", short.class),
            Map.entry("int32", int.class),
            Map.entry("int64", long.class),
            Map.entry("String", String.class),
            Map.entry("WString", String.class)
        );
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
