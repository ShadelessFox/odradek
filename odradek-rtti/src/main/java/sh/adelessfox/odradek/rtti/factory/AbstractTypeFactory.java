package sh.adelessfox.odradek.rtti.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.rtti.ClassAttrInfo;
import sh.adelessfox.odradek.rtti.ClassBaseInfo;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.rtti.runtime.TypeContext;
import sh.adelessfox.odradek.rtti.runtime.TypeRuntimeGenerator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.net.URL;
import java.util.*;

public abstract class AbstractTypeFactory implements TypeFactory {
    private static final Logger log = LoggerFactory.getLogger(AbstractTypeFactory.class);

    private final Map<ClassTypeInfo, Class<?>> classes = new IdentityHashMap<>();
    private final Map<TypeId, TypeInfo> types = new HashMap<>();

    private final TypeContext context;
    private final TypeRuntimeGenerator generator;

    public AbstractTypeFactory(Class<?> namespace, MethodHandles.Lookup lookup) {
        try {
            log.debug("Loading type context");
            context = new FactoryTypeContext();
            context.load(getTypes(), getExtensions());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        log.debug("Initializing type generator");
        generator = new TypeRuntimeGenerator(lookup, namespace.getPackageName(), namespace.getSimpleName());
        generator.addBuiltins(getBuiltins());

        log.debug("Computing type ids");
        context.getAll().forEach(info -> {
            TypeId id = computeTypeId(info);
            if (types.putIfAbsent(id, info) != null) {
                throw new IllegalStateException("Duplicate type id " + id + " for " + types.get(id) + " and " + info);
            }
        });
    }

    @Override
    public TypeInfo get(String name) {
        return context.get(name)
            .orElseThrow(() -> new TypeNotFoundException("Unknown type: " + name));
    }

    @Override
    public TypeInfo get(TypeId id) {
        TypeInfo info = types.get(id);
        if (info == null) {
            throw new TypeNotFoundException("Unknown type: " + id);
        }
        return info;
    }

    @Override
    @SuppressWarnings({"deprecation", "unchecked"})
    public <T> T newInstance(ClassTypeInfo info) {
        Class<?> clazz = classes.computeIfAbsent(info, generator::lookup);
        try {
            return (T) clazz.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
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

    protected abstract TypeId computeTypeId(TypeInfo info);

    protected abstract void sortOrderedAttributes(List<OrderedAttr> attrs);

    protected abstract void filterOrderedAttributes(List<OrderedAttr> attrs);

    protected abstract URL getTypes();

    protected abstract URL getExtensions();

    protected abstract Map<String, Class<?>> getBuiltins();

    protected record OrderedAttr(ClassTypeInfo parent, ClassAttrInfo attr, int offset) {
    }

    private final class FactoryTypeContext extends TypeContext {
        @Override
        protected Class<?> computeType(TypeInfo info) {
            return generator.getType(info);
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
