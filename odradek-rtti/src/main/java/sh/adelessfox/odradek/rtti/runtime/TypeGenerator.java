package sh.adelessfox.odradek.rtti.runtime;

import sh.adelessfox.odradek.rtti.ClassAttrInfo;
import sh.adelessfox.odradek.rtti.ClassBaseInfo;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;

import javax.lang.model.SourceVersion;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TypeGenerator<T> {
    private static final Set<String> OBJECT_METHODS = Set.of("hashCode", "equals", "clone", "notify", "notifyAll", "wait", "finalize");

    private final Map<ClassTypeInfo, List<ClassGroupInfo>> groups = new IdentityHashMap<>();
    private final Map<ClassTypeInfo, List<ClassAttrInfo>> attributes = new IdentityHashMap<>();
    private final Map<String, T> builtins = new HashMap<>();
    private final Map<String, T> callbacks = new HashMap<>();
    private final Map<String, T> extensions = new HashMap<>();

    public void addBuiltins(Map<? extends String, ? extends T> builtins) {
        builtins.forEach(this::addBuiltin);
    }

    public void addBuiltin(String name, T type) {
        if (builtins.putIfAbsent(name, type) != null) {
            throw new IllegalStateException("Builtin for type '" + name + "' already exists");
        }
    }

    public void addCallbacks(Map<? extends String, ? extends T> callbacks) {
        callbacks.forEach(this::addCallback);
    }

    public void addCallback(String name, T type) {
        if (callbacks.putIfAbsent(name, type) != null) {
            throw new IllegalStateException("Callback for type '" + name + "' already exists");
        }
    }

    public void addExtensions(Map<? extends String, ? extends T> extensions) {
        extensions.forEach(this::addExtension);
    }

    public void addExtension(String name, T type) {
        if (extensions.putIfAbsent(name, type) != null) {
            throw new IllegalStateException("Extension for type '" + name + "' already exists");
        }
    }

    public Optional<T> getBuiltin(String name) {
        return Optional.ofNullable(builtins.get(name));
    }

    public Optional<T> getCallback(String name) {
        return Optional.ofNullable(callbacks.get(name));
    }

    public Optional<T> getExtension(String name) {
        return Optional.ofNullable(extensions.get(name));
    }

    // region Name conversion
    protected String toGetterName(ClassGroupInfo group) {
        return toJavaIdentifier(group.name());
    }

    protected String toGetterName(ClassAttrInfo attr) {
        return toJavaIdentifier(attr.name());
    }

    protected String toSetterName(ClassAttrInfo attr) {
        return toJavaIdentifier(attr.name());
    }

    protected String toTypeName(ClassGroupInfo group) {
        return group.name() + "Group";
    }

    protected String toJavaIdentifier(String value) {
        return normalizeCase(value);
    }

    private String normalizeCase(String name) {
        if (name.length() > 1 && name.matches("^m[A-Z].*$")) {
            name = name.substring(1);
        }
        if (name.indexOf(1, '_') > 0) {
            name = Arrays.stream(name.split("_"))
                .filter(part -> !part.isBlank())
                .map(TypeGenerator::capitalize)
                .collect(Collectors.joining());
        }
        if (name.chars().allMatch(Character::isUpperCase)) {
            name = name.toLowerCase(Locale.ROOT);
        } else {
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }
        if (!SourceVersion.isIdentifier(name)) {
            name = '_' + name;
        }
        if (SourceVersion.isIdentifier(name)) {
            if (SourceVersion.isKeyword(name) || OBJECT_METHODS.contains(name)) {
                name += '_';
            }
            return name;
        }
        throw new IllegalStateException("Invalid method name: " + name);
    }

    private static String capitalize(String name) {
        if (name.length() >= 2 && Character.isLowerCase(name.charAt(0)) && Character.isUpperCase(name.charAt(1))) {
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
        return name;
    }
    // endregion

    protected List<ClassGroupInfo> collectGroups(ClassTypeInfo info) {
        return groups.computeIfAbsent(info, info1 -> {
            var declared = info1.attrs().stream()
                .map(ClassAttrInfo::group).flatMap(Optional::stream)
                .distinct()
                .map(group -> new ClassGroupInfo(group, info1, List.of()))
                .collect(Collectors.toMap(ClassGroupInfo::name, Function.identity()));
            var merged = new TreeMap<>(declared);

            for (ClassBaseInfo base : info1.bases()) {
                for (ClassGroupInfo group : collectGroups(base.type())) {
                    var declaredGroup = declared.get(group.name());
                    var mergedGroup = merged.get(group.name());

                    if (mergedGroup == null) {
                        merged.put(group.name(), group);
                    } else if (declaredGroup == null) {
                        merged.put(group.name(), new ClassGroupInfo(group.name(), info1, List.of(mergedGroup.host(), group.host())));
                    } else {
                        merged.put(group.name(), mergedGroup.inherit(group.host()));
                    }
                }
            }

            return List.copyOf(merged.values());
        });
    }

    protected List<ClassAttrInfo> collectAttributes(ClassTypeInfo info) {
        return attributes.computeIfAbsent(info, info1 -> {
            var attrs = new ArrayList<ClassAttrInfo>();
            for (ClassBaseInfo base : info1.bases()) {
                attrs.addAll(collectAttributes(base.type()));
            }
            attrs.addAll(info1.attrs());
            return List.copyOf(attrs);
        });
    }

    protected record ClassGroupInfo(String name, ClassTypeInfo host, List<ClassTypeInfo> inherits) {
        private ClassGroupInfo inherit(ClassTypeInfo info) {
            return new ClassGroupInfo(
                name,
                host,
                Stream.concat(inherits.stream(), Stream.of(info)).toList()
            );
        }
    }
}
