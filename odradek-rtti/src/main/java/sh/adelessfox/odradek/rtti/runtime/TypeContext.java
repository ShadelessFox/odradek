package sh.adelessfox.odradek.rtti.runtime;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.rtti.*;
import sh.adelessfox.odradek.rtti.data.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TypeContext {
    private static final Logger log = LoggerFactory.getLogger(TypeContext.class);

    private final Map<String, TypeInfo> types = new TreeMap<>();
    private final Map<StableValue<TypeInfo>, String> pending = new IdentityHashMap<>();
    private final Map<String, List<ClassTypeInfo>> extensions = new HashMap<>();

    public void load(URL typesUrl, URL extensionsUrl) throws IOException {
        var typesObject = readJson(typesUrl);
        var extensionsObject = readJson(extensionsUrl);

        log.debug("Loading type extensions");
        for (var entry : extensionsObject.getAsJsonObject("types").entrySet()) {
            var typeName = entry.getKey();
            var typeInfo = process(typeName, entry.getValue().getAsJsonObject());
            types.put(typeName, typeInfo);
        }

        for (var entry : extensionsObject.getAsJsonObject("extends").entrySet()) {
            var typeName = entry.getKey();
            var typeExtensions = StreamSupport.stream(entry.getValue().getAsJsonArray().spliterator(), false)
                .map(element -> (ClassTypeInfo) Objects.requireNonNull(types.get(element.getAsString())))
                .toList();
            extensions.put(typeName, typeExtensions);
        }

        log.debug("Loading type definitions");
        for (var entry : typesObject.entrySet()) {
            var typeName = entry.getKey();
            var typeInfo = process(typeName, entry.getValue().getAsJsonObject());
            types.put(typeName, typeInfo);
        }

        log.debug("Resolving type references");
        pending.forEach((holder, name) -> {
            var type = Objects.requireNonNull(types.get(name), "Couldn't find type '" + name + "'");
            holder.setOrThrow(type);
        });

        // Cleanup
        pending.clear();
        extensions.clear();
    }

    private static JsonObject readJson(URL url) throws IOException {
        try (InputStream is = url.openStream()) {
            var reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    public Optional<TypeInfo> get(String name) {
        return Optional.ofNullable(types.get(name));
    }

    public Stream<TypeInfo> getAll() {
        return types.values().stream();
    }

    private TypeInfo process(String name, JsonObject object) {
        var kind = object.get("kind").getAsString();
        return switch (kind) {
            case "atom" -> processAtom(name, object);
            case "compound" -> processCompound(name, object);
            case "container" -> processContainer(name, object);
            case "enum flags" -> processEnum(name, object, true);
            case "enum" -> processEnum(name, object, false);
            case "pointer" -> processPointer(name, object);
            default -> throw new IllegalStateException("Type '%s' has unsupported kind '%s'".formatted(name, kind));
        };
    }

    private AtomTypeInfo processAtom(String name, JsonObject object) {
        var baseType = object.get("base_type").getAsString();
        return new AtomTypeInfoImpl(name, resolveType(baseType));
    }

    private ClassTypeInfoImpl processCompound(String name, JsonObject object) {
        var messages = new ArrayList<String>();
        var bases = new ArrayList<ClassBaseInfo>();
        var attrs = new ArrayList<ClassAttrInfo>();
        var version = object.get("version").getAsInt();
        var flags = object.get("flags").getAsInt();

        if (object.has("messages")) {
            for (JsonElement element : object.getAsJsonArray("messages")) {
                messages.add(element.getAsString());
            }
        }

        if (object.has("bases")) {
            for (JsonElement element : object.getAsJsonArray("bases")) {
                var base = element.getAsJsonObject();
                var baseType = base.get("type").getAsString();
                var baseOffset = base.get("offset").getAsInt();

                bases.add(new ClassBaseInfoImpl(
                    resolveType(baseType),
                    baseOffset
                ));
            }
        }

        if (object.has("attrs")) {
            String attrGroup = null;
            for (JsonElement element : object.getAsJsonArray("attrs")) {
                var attr = element.getAsJsonObject();
                if (attr.has("category")) {
                    attrGroup = attr.get("category").getAsString();
                    continue;
                }

                var attrName = attr.get("name").getAsString();
                var attrType = attr.get("type").getAsString();
                var attrOffset = attr.get("offset").getAsInt();
                var attrFlags = attr.get("flags").getAsInt();
                var attrMin = attr.has("min") ? attr.get("min").getAsString() : null;
                var attrMax = attr.has("max") ? attr.get("max").getAsString() : null;
                var attrProperty = attr.has("property") && attr.get("property").getAsBoolean();

                attrs.add(new ClassAttrInfoImpl(
                    attrName,
                    attrGroup,
                    resolveType(attrType),
                    attrMin,
                    attrMax,
                    attrOffset,
                    attrFlags,
                    attrProperty
                ));
            }
        }

        List<ClassTypeInfo> extensions = this.extensions.get(name);
        if (extensions != null) {
            for (ClassTypeInfo extension : extensions) {
                bases.add(new ClassBaseInfoImpl(
                    StableValue.of(extension),
                    -1
                ));
            }
        }

        return new ClassTypeInfoImpl(name, bases, attrs, messages, version, flags);
    }

    private ContainerTypeInfo processContainer(String name, JsonObject object) {
        var type = object.get("type").getAsString();
        var itemType = object.get("item_type").getAsString();

        return new ContainerTypeInfoImpl(
            name,
            type,
            resolveType(itemType)
        );
    }

    private EnumTypeInfo processEnum(String name, JsonObject object, boolean asEnumSet) {
        var size = object.get("size").getAsInt();
        var values = new ArrayList<EnumValueInfo>();

        if (object.has("values")) {
            for (JsonElement element : object.get("values").getAsJsonArray()) {
                var value = element.getAsJsonObject();
                var valueName = value.get("name").getAsString();
                var valueValue = value.get("value").getAsInt();
                var valueAliases = value.has("alias") ? value.getAsJsonArray("alias").asList().stream().map(JsonElement::getAsString).toList() : List.<String>of();

                values.add(new EnumValueInfoImpl(
                    valueName,
                    valueAliases,
                    valueValue
                ));
            }
        }

        if (asEnumSet) {
            return new EnumSetTypeInfoImpl(name, values, size);
        } else {
            return new EnumTypeInfoImpl(name, values, size);
        }
    }

    private PointerTypeInfo processPointer(String name, JsonObject object) {
        var type = object.get("type").getAsString();
        var itemType = object.get("item_type").getAsString();

        return new PointerTypeInfoImpl(
            name,
            type,
            resolveType(itemType)
        );
    }

    private StableValue<TypeInfo> resolveType(String type) {
        StableValue<TypeInfo> holder = StableValue.of();
        pending.put(holder, type);
        return holder;
    }

    protected Class<?> computeType(TypeInfo info) {
        throw new UnsupportedOperationException("Can't compute type in the current context");
    }

    protected VarHandle computeHandle(ClassTypeInfo info, ClassAttrInfo attr) {
        throw new UnsupportedOperationException("Can't compute handle in the current context");
    }

    protected List<ClassAttrInfo> computeOrderedAttrs(ClassTypeInfo info) {
        throw new UnsupportedOperationException("Can't compute ordered attrs in the current context");
    }

    private sealed class TypeInfoImpl {
        private final String name;
        private final StableValue<Class<?>> type = StableValue.of();

        TypeInfoImpl(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

        public Class<?> type() {
            return type.orElseSet(() -> computeType((TypeInfo) this));
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final class AtomTypeInfoImpl extends TypeInfoImpl implements AtomTypeInfo {
        private final StableValue<TypeInfo> base;

        AtomTypeInfoImpl(String name, StableValue<TypeInfo> base) {
            super(name);
            this.base = base;
        }

        @Override
        public AtomTypeInfo base() {
            return (AtomTypeInfo) base.orElseThrow();
        }
    }

    private static final class ClassBaseInfoImpl implements ClassBaseInfo {
        private final StableValue<TypeInfo> type;
        private final int offset;

        ClassBaseInfoImpl(StableValue<TypeInfo> type, int offset) {
            this.type = type;
            this.offset = offset;
        }

        @Override
        public ClassTypeInfo type() {
            return (ClassTypeInfo) type.orElseThrow();
        }

        @Override
        public int offset() {
            return offset;
        }

        @Override
        public String toString() {
            return type().name();
        }
    }

    private static final class ClassAttrInfoImpl implements ClassAttrInfo {
        private final String name;
        private final String group;
        private final StableValue<TypeInfo> type;
        private final String min;
        private final String max;
        private final int offset;
        private final int flags;
        private final boolean property;

        ClassAttrInfoImpl(
            String name,
            String group,
            StableValue<TypeInfo> type,
            String min,
            String max,
            int offset,
            int flags,
            boolean property
        ) {
            this.name = name;
            this.group = group;
            this.type = type;
            this.min = min;
            this.max = max;
            this.offset = offset;
            this.flags = flags;
            this.property = property;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public Optional<String> group() {
            return Optional.ofNullable(group);
        }

        @Override
        public TypeInfo type() {
            return type.orElseThrow();
        }

        @Override
        public Optional<String> min() {
            return Optional.ofNullable(min);
        }

        @Override
        public Optional<String> max() {
            return Optional.ofNullable(max);
        }

        @Override
        public int offset() {
            return offset;
        }

        @Override
        public int flags() {
            return flags;
        }

        @Override
        public boolean isProperty() {
            return property;
        }

        @Override
        public String toString() {
            return group != null ? group + '.' + name : name;
        }
    }

    private final class ClassTypeInfoImpl extends TypeInfoImpl implements ClassTypeInfo {
        private final List<ClassBaseInfo> bases;
        private final List<ClassAttrInfo> attrs;
        private final StableValue<List<ClassAttrInfo>> orderedAttrs;
        private final List<String> messages;
        private final Map<ClassAttrInfo, VarHandle> handles = new HashMap<>();
        private final int version;
        private final int flags;

        ClassTypeInfoImpl(
            String name,
            List<ClassBaseInfo> bases,
            List<ClassAttrInfo> attrs,
            List<String> messages,
            int version,
            int flags
        ) {
            super(name);
            this.bases = List.copyOf(bases);
            this.attrs = List.copyOf(attrs);
            this.orderedAttrs = StableValue.of();
            this.messages = List.copyOf(messages);
            this.version = version;
            this.flags = flags;
        }

        @Override
        public List<ClassBaseInfo> bases() {
            return bases;
        }

        @Override
        public List<ClassAttrInfo> attrs() {
            return attrs;
        }

        @Override
        public List<ClassAttrInfo> orderedAttrs() {
            return orderedAttrs.orElseSet(() -> computeOrderedAttrs(this));
        }

        @Override
        public List<String> messages() {
            return messages;
        }

        @Override
        public int version() {
            return version;
        }

        @Override
        public int flags() {
            return flags;
        }

        @Override
        public VarHandle handle(ClassAttrInfo attr) {
            return handles.computeIfAbsent(attr, attr1 -> computeHandle(this, attr1));
        }
    }

    private final class ContainerTypeInfoImpl extends TypeInfoImpl implements ContainerTypeInfo {
        private final String type;
        private final StableValue<TypeInfo> itemType;

        ContainerTypeInfoImpl(String name, String type, StableValue<TypeInfo> itemType) {
            super(name);
            this.type = type;
            this.itemType = itemType;
        }

        @Override
        public String containerType() {
            return type;
        }

        @Override
        public TypeInfo itemType() {
            return itemType.orElseThrow();
        }

        @Override
        public Object newInstance(int length) {
            if (isArray()) {
                return Array.newInstance(itemType().type(), length);
            } else {
                return Arrays.asList(new Object[length]);
            }
        }

        @Override
        public Object get(Object object, int index) {
            if (isArray()) {
                return Array.get(object, index);
            } else {
                return ((List<?>) object).get(index);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void set(Object object, int index, Object value) {
            if (isArray()) {
                Array.set(object, index, value);
            } else {
                ((List<Object>) object).set(index, value);
            }
        }

        @Override
        public int length(Object object) {
            if (isArray()) {
                return Array.getLength(object);
            } else {
                return ((List<?>) object).size();
            }
        }

        private boolean isArray() {
            return itemType().type().isPrimitive();
        }
    }

    private static final class EnumValueInfoImpl implements EnumValueInfo {
        private final String name;
        private final List<String> aliases;
        private final int value;

        EnumValueInfoImpl(String name, List<String> aliases, int value) {
            this.name = name;
            this.aliases = List.copyOf(aliases);
            this.value = value;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public int value() {
            return value;
        }

        @Override
        public List<String> aliases() {
            return aliases;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private sealed class EnumTypeInfoImpl extends TypeInfoImpl implements EnumTypeInfo {
        private final List<EnumValueInfo> values;
        private final int size;

        EnumTypeInfoImpl(String name, List<EnumValueInfo> values, int size) {
            super(name);
            this.values = List.copyOf(values);
            this.size = size;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public List<EnumValueInfo> values() {
            return values;
        }

        @Override
        public Class<?> type() {
            return Value.class; // FIXME seems weird
        }

        @SuppressWarnings("unchecked")
        public <T extends Enum<T> & Value.OfEnum<T>> Value.OfEnum<T> valueOf(int value) {
            return Value.valueOf((Class<T>) enumType(), value);
        }

        public <T extends Enum<T> & Value.OfEnumSet<T>> Value.OfEnumSet<T> setOf(int value) {
            throw new IllegalStateException("Not an enum set");
        }

        Class<?> enumType() {
            return super.type();  // FIXME seems weird
        }
    }

    private final class EnumSetTypeInfoImpl extends EnumTypeInfoImpl implements EnumSetTypeInfo {
        EnumSetTypeInfoImpl(String name, List<EnumValueInfo> values, int size) {
            super(name, values, size);
        }

        @Override
        public <T extends Enum<T> & Value.OfEnum<T>> Value.OfEnum<T> valueOf(int value) {
            throw new IllegalStateException("Not an enum");
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends Enum<T> & Value.OfEnumSet<T>> Value.OfEnumSet<T> setOf(int value) {
            return Value.setOf((Class<T>) enumType(), value);
        }
    }

    private final class PointerTypeInfoImpl extends TypeInfoImpl implements PointerTypeInfo {
        private final String type;
        private final StableValue<TypeInfo> itemType;

        PointerTypeInfoImpl(String name, String type, StableValue<TypeInfo> itemType) {
            super(name);
            this.type = type;
            this.itemType = itemType;
        }

        @Override
        public String pointerType() {
            return type;
        }

        @Override
        public TypeInfo itemType() {
            return itemType.orElseThrow();
        }
    }
}
