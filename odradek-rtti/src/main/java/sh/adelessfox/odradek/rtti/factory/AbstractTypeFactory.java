package sh.adelessfox.odradek.rtti.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.rtti.ClassAttrInfo;
import sh.adelessfox.odradek.rtti.ClassBaseInfo;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.rtti.generator.TypeBindings;
import sh.adelessfox.odradek.rtti.generator.TypeContext;
import sh.adelessfox.odradek.rtti.generator.TypeRuntimeGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractTypeFactory<T extends TypeId> implements TypeFactory {
    private static final Logger log = LoggerFactory.getLogger(AbstractTypeFactory.class);

    private final Map<T, TypeInfo> types = new HashMap<>();
    private final Map<TypeInfo, T> ids = new IdentityHashMap<>();
    private final Class<?> namespace;
    private final TypeContext context;
    private final TypeRuntimeGenerator generator;

    protected AbstractTypeFactory(Class<?> namespace, MethodHandles.Lookup lookup) {
        this.namespace = namespace;

        generator = new TypeRuntimeGenerator(lookup, namespace.getPackageName(), namespace.getSimpleName());
        generator.addBuiltins(getBuiltins());
        context = new FactoryTypeContext();

        try {
            log.debug("Loading type context");
            context.load(getTypes(), getExtensions());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        log.debug("Computing type ids");
        for (TypeInfo info : context.getAll()) {
            var id = computeTypeId(info);
            if (types.putIfAbsent(id, info) != null) {
                throw new IllegalStateException("Duplicate type id " + id + " for " + types.get(id) + " and " + info);
            }
            ids.put(info, id);
        }
    }

    @Override
    public TypeInfo get(String name) {
        return context.get(name)
            .orElseThrow(() -> new TypeNotFoundException("Unknown type: " + name));
    }

    @Override
    public TypeInfo get(TypeId id) {
        @SuppressWarnings("unchecked")
        var info = types.get((T) id);
        if (info == null) {
            throw new TypeNotFoundException("Unknown type: " + id);
        }
        return info;
    }

    @Override
    public Collection<TypeInfo> getAll() {
        return context.getAll();
    }

    @Override
    public T getId(TypeInfo info) {
        var id = ids.get(info);
        if (id == null) {
            throw new TypeNotFoundException("Unknown type: " + info);
        }
        return id;
    }

    private static void collectOrderedAttrs(ClassTypeInfo info, int offset, List<OrderedAttr> attrs) {
        for (ClassBaseInfo base : info.bases()) {
            if (base.offset() < 0) {
                // Extension type, see TypeContext#processCompound
                continue;
            }
            collectOrderedAttrs(base.type(), offset + base.offset(), attrs);
        }
        for (ClassAttrInfo attr : info.attrs()) {
            attrs.add(new OrderedAttr(info, attr, offset + attr.offset()));
        }
    }

    protected abstract T computeTypeId(TypeInfo info);

    protected abstract void sortOrderedAttributes(List<OrderedAttr> attrs);

    protected abstract void filterOrderedAttributes(List<OrderedAttr> attrs);

    protected InputStream getTypes() throws IOException {
        return namespace.getModule().getResourceAsStream(getAnnotation().input().types());
    }

    protected InputStream getExtensions() throws IOException {
        return namespace.getModule().getResourceAsStream(getAnnotation().input().extensions());
    }

    protected Map<String, Class<?>> getBuiltins() {
        return Stream.of(getAnnotation().builtins())
            .collect(Collectors.toMap(TypeBindings.Builtin::type, TypeBindings.Builtin::repr));
    }

    private TypeBindings getAnnotation() {
        return namespace.getModule().getDeclaredAnnotation(TypeBindings.class);
    }

    protected record OrderedAttr(ClassTypeInfo parent, ClassAttrInfo attr, int offset) {
    }

    private final class FactoryTypeContext extends TypeContext {
        @Override
        protected Class<?> computeType(TypeInfo info) {
            return generator.getType(info);
        }

        @Override
        protected Object newInstance(ClassTypeInfo info) {
            return generator.newInstance(info);
        }

        @Override
        protected VarHandle computeHandle(ClassTypeInfo info, ClassAttrInfo attr) {
            return generator.getHandle(info, attr);
        }

        @Override
        protected List<ClassAttrInfo> computeOrderedAttrs(ClassTypeInfo info) {
            var attrs = new ArrayList<OrderedAttr>();

            collectOrderedAttrs(info, 0, attrs);
            sortOrderedAttributes(attrs);
            filterOrderedAttributes(attrs);

            return attrs.stream()
                .map(OrderedAttr::attr)
                .toList();
        }
    }
}
