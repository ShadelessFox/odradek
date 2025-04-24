package sh.adelessfox.odradek.rtti.generator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import sh.adelessfox.odradek.rtti.factory.TypeName;
import sh.adelessfox.odradek.rtti.generator.data.*;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

class TypeContext {
    private final Map<TypeName, TypeInfo> types = new TreeMap<>(Comparator.comparing(TypeName::fullName, String::compareToIgnoreCase));

    public void load(Reader reader) throws IOException {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

        Resolver resolver = new Resolver() {
            private final Map<TypeName, FutureRef> pending = new HashMap<>();

            @Override
            public TypeInfoRef get(TypeName name) {
                if (pending.containsKey(name)) {
                    return pending.get(name);
                }

                TypeInfo type = types.get(name);
                if (type != null) {
                    return new ResolvedRef(type);
                }

                pending.put(name, new FutureRef(name));

                if (name instanceof TypeName.Parameterized(String raw, TypeName argument)) {
                    return resolve(getParameterizedType(raw, argument));
                } else {
                    return resolve(getSimpleType(name.fullName()));
                }
            }

            private TypeInfo getSimpleType(String name) {
                JsonObject info = root.getAsJsonObject(name);
                if (info == null) {
                    throw new IllegalArgumentException("Unknown type: " + name);
                }
                return loadSingleType(name, info, this);
            }

            private TypeInfo getParameterizedType(String name, TypeName argument) {
                JsonObject info = root.getAsJsonObject(name);
                if (info == null) {
                    throw new IllegalArgumentException("Unknown template type: " + name);
                }
                return loadParameterizedType(name, argument, info, this);
            }

            private TypeInfoRef resolve(TypeInfo info) {
                FutureRef ref = pending.remove(info.typeName());
                if (ref == null) {
                    throw new IllegalStateException("Type was not present in the queue: " + info.typeName());
                }
                if (types.put(info.typeName(), info) != null) {
                    throw new IllegalStateException("Type was already resolved: " + info.typeName());
                }
                ref.resolved = info;
                return ref;
            }
        };

        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            var name = entry.getKey();
            if (name.startsWith("$")) {
                continue;
            }
            var object = entry.getValue().getAsJsonObject();
            var kind = object.get("kind").getAsString();
            if (kind.equals("pointer") || kind.equals("container")) {
                // The dump doesn't contain their specializations, they are resolved on demand in #loadParameterizedType
                continue;
            }
            resolver.get(TypeName.of(name));
        }
    }

    public Collection<TypeInfo> types() {
        return Collections.unmodifiableCollection(types.values());
    }

    private static TypeInfo loadSingleType(String name, JsonObject object, Resolver resolver) {
        String kind = object.get("kind").getAsString();
        return switch (kind) {
            case "primitive" -> loadAtomType(name, object, resolver);
            case "enum" -> loadEnumType(name, object, false);
            case "enum flags" -> loadEnumType(name, object, true);
            case "class" -> loadCompoundType(name, object, resolver);
            default -> throw new IllegalArgumentException("Unexpected kind of type: " + kind);
        };
    }

    private TypeInfo loadParameterizedType(String name, TypeName argument, JsonObject object, Resolver resolver) {
        String kind = object.get("kind").getAsString();
        return switch (kind) {
            case "pointer" -> new PointerTypeInfo(name, resolver.get(argument));
            case "container" -> new ContainerTypeInfo(name, resolver.get(argument));
            default -> throw new IllegalArgumentException("Unexpected kind of type: " + kind);
        };
    }

    private static TypeInfo loadAtomType(String name, JsonObject object, Resolver resolver) {
        String base = object.get("base_type").getAsString();
        Optional<TypeInfo> parent = base.equals(name) ? Optional.empty() : Optional.of(resolver.get(TypeName.of(base)).value());
        return new AtomTypeInfo(name, parent);
    }

    private static TypeInfo loadEnumType(String name, JsonObject object, boolean flags) {
        List<EnumValueInfo> values = new ArrayList<>();
        for (JsonElement element : object.getAsJsonArray("values")) {
            JsonObject value = element.getAsJsonObject();
            values.add(new EnumValueInfo(
                value.get("name").getAsString(),
                value.get("value").getAsInt()
            ));
        }

        EnumValueSize size = switch (object.get("size").getAsInt()) {
            case 1 -> EnumValueSize.INT8;
            case 2 -> EnumValueSize.INT16;
            case 4 -> EnumValueSize.INT32;
            default -> throw new IllegalArgumentException("Invalid enum size: " + object.get("size").getAsInt());
        };

        return new EnumTypeInfo(
            name,
            values,
            size,
            flags
        );
    }

    private static TypeInfo loadCompoundType(String name, JsonObject object, Resolver resolver) {
        List<ClassBaseInfo> bases = new ArrayList<>();
        if (object.has("bases")) {
            for (JsonElement element : object.getAsJsonArray("bases")) {
                JsonObject base = element.getAsJsonObject();
                TypeName baseName = TypeName.of(base.get("name").getAsString());
                bases.add(new ClassBaseInfo(
                    resolver.get(baseName),
                    base.get("offset").getAsInt()
                ));
            }
        }

        List<ClassAttrInfo> attrs = new ArrayList<>();
        if (object.has("attrs")) {
            String category = null;
            for (JsonElement element : object.getAsJsonArray("attrs")) {
                JsonObject attr = element.getAsJsonObject();
                if (attr.has("category")) {
                    category = attr.get("category").getAsString();
                    continue;
                }
                attrs.add(new ClassAttrInfo(
                    attr.get("name").getAsString(),
                    Optional.ofNullable(category),
                    resolver.get(TypeName.parse(attr.get("type").getAsString())),
                    Optional.ofNullable(attr.get("min")).map(JsonElement::getAsString),
                    Optional.ofNullable(attr.get("max")).map(JsonElement::getAsString),
                    attrs.size(),
                    attr.get("offset").getAsInt(),
                    attr.get("flags").getAsInt(),
                    attr.has("property")
                ));
            }
        }

        Set<String> messages = new HashSet<>();
        if (object.has("messages")) {
            for (JsonElement element : object.getAsJsonArray("messages")) {
                messages.add(element.getAsString());
            }
        }

        return new ClassTypeInfo(
            name,
            bases,
            attrs,
            messages,
            object.has("version") ? object.get("version").getAsInt() : 0,
            object.has("flags") ? object.get("flags").getAsInt() : 0
        );
    }

    private interface Resolver {
        TypeInfoRef get(TypeName name);
    }

    private record ResolvedRef(TypeInfo value) implements TypeInfoRef {
        @Override
        public TypeName typeName() {
            return value.typeName();
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }

    private static class FutureRef implements TypeInfoRef {
        private final TypeName name;
        private TypeInfo resolved;

        public FutureRef(TypeName name) {
            this.name = name;
        }

        @Override
        public TypeName typeName() {
            return name;
        }

        @Override
        public TypeInfo value() {
            if (resolved == null) {
                throw new IllegalStateException("Type '" + name + "' is not resolved");
            }
            return resolved;
        }

        @Override
        public String toString() {
            return resolved != null ? resolved.toString() : "<pending>";
        }
    }
}
