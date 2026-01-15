package sh.adelessfox.odradek.rtti.runtime;

import sh.adelessfox.odradek.rtti.*;
import sh.adelessfox.odradek.rtti.data.Ref;
import sh.adelessfox.odradek.rtti.data.Value;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.TypeKind;
import java.lang.classfile.attribute.*;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.AccessFlag;
import java.util.*;
import java.util.function.Consumer;

import static java.lang.classfile.ClassFile.*;
import static java.lang.constant.ConstantDescs.*;

public final class TypeRuntimeGenerator extends TypeGenerator<Class<?>> {
    private static final ClassDesc CD_Ref = Ref.class.describeConstable().orElseThrow();
    private static final ClassDesc CD_Value = Value.class.describeConstable().orElseThrow();
    private static final ClassDesc CD_List = List.class.describeConstable().orElseThrow();
    private static final ClassDesc CD_StableValue = StableValue.class.describeConstable().orElseThrow();
    private static final ClassDesc CD_ClassTypeInfo = ClassTypeInfo.class.describeConstable().orElseThrow();
    private static final ClassDesc CD_UnsupportedOperationException = UnsupportedOperationException.class.describeConstable().orElseThrow();

    private final Map<ClassTypeInfo, Class<?>> classes = new IdentityHashMap<>();
    private final MethodHandles.Lookup lookup;
    private final String packageName;
    private final String className;

    public TypeRuntimeGenerator(MethodHandles.Lookup lookup, String packageName, String className) {
        this.lookup = lookup;
        this.packageName = packageName;
        this.className = className;
    }

    public Class<?> lookup(ClassTypeInfo info) {
        return classes.computeIfAbsent(info, this::generateClass);
    }

    public Class<?> getType(TypeInfo info) {
        try {
            return toClassDesc(info, false).resolveConstantDesc(lookup);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public VarHandle getHandle(ClassTypeInfo info, ClassAttrInfo attr) {
        var clazz = lookup(info);
        try {
            var type = toClassDesc(attr.type(), true).resolveConstantDesc(lookup);
            return MethodHandles
                .privateLookupIn(clazz, lookup)
                .findVarHandle(clazz, toFieldName(attr), type);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private Class<?> generateClass(ClassTypeInfo info) {
        var desc = toImplClassDesc(info);
        var data = ClassFile.of().build(desc, cb -> {
            cb.withFlags(AccessFlag.PUBLIC, AccessFlag.FINAL);
            cb.withInterfaceSymbols(toClassDesc(info));

            // Type
            cb.withField("$type", CD_StableValue, ACC_STATIC | ACC_FINAL | ACC_SYNTHETIC);
            cb.withMethodBody(CLASS_INIT_NAME, MTD_void, ACC_STATIC, cob -> cob
                .invokestatic(CD_StableValue, "of", MethodTypeDesc.of(CD_StableValue), true)
                .putstatic(desc, "$type", CD_StableValue)
                .return_());
            cb.withMethodBody("getType", MethodTypeDesc.of(CD_ClassTypeInfo), ClassFile.ACC_PUBLIC, cob -> cob
                .getstatic(desc, "$type", CD_StableValue)
                .invokeinterface(CD_StableValue, "orElseThrow", MethodTypeDesc.of(CD_Object))
                .checkcast(CD_ClassTypeInfo)
                .areturn());

            List<Consumer<CodeBuilder>> constructor = new ArrayList<>(1);
            List<ClassDesc> groupClasses = new ArrayList<>();

            // Groups
            for (ClassGroupInfo group : collectGroups(info)) {
                var groupField = toFieldName(group);
                var groupDesc = toClassDesc(group);
                var groupImplDesc = desc.nested(toTypeName(group));

                // Let the host class know about ourselves
                groupClasses.add(groupImplDesc);

                // Field
                cb.withField(groupField, groupDesc, ACC_FINAL | ACC_SYNTHETIC);

                // Getter
                cb.withMethodBody(toGetterName(group), MethodTypeDesc.of(groupDesc), ClassFile.ACC_PUBLIC, cob -> cob
                    .aload(0)
                    .getfield(desc, groupField, groupDesc)
                    .areturn());

                // Constructor initializer
                constructor.add(cob -> cob
                    .aload(0)
                    .new_(groupImplDesc)
                    .dup()
                    .aload(0)
                    .invokespecial(groupImplDesc, INIT_NAME, MethodTypeDesc.of(CD_void, desc))
                    .putfield(desc, groupField, groupDesc));

                // Class
                generateGroupClass(info, desc, group, groupDesc, groupImplDesc);
            }

            // Integrate information about nested group classes
            cb.with(NestMembersAttribute.ofSymbols(groupClasses));
            cb.with(InnerClassesAttribute.of(groupClasses.stream()
                .map(d -> InnerClassInfo.of(d, Optional.empty(), Optional.empty()))
                .toList()));

            // Attributes
            for (ClassAttrInfo attr : collectAttributes(info)) {
                var attrField = toFieldName(attr);
                var attrDesc = toClassDesc(attr.type(), true);

                if (attr.isSerialized() || !attr.isProperty()) {
                    // Non-serialized property values are evaluated via functions; they don't have any explicit storage.
                    cb.withField(attrField, attrDesc, ClassFile.ACC_PRIVATE);
                }

                // Getter and setter, if no group
                if (attr.group().isEmpty()) {
                    buildAttr(cb, desc, attr);
                }
            }

            // Constructor
            cb.withMethodBody(INIT_NAME, MTD_void, ClassFile.ACC_PUBLIC, cob -> {
                cob.aload(0);
                cob.invokespecial(CD_Object, INIT_NAME, MTD_void);

                constructor.forEach(c -> c.accept(cob));

                cob.return_();
            });
        });

        try {
            var thisClass = lookup.findClass(packageName + '.' + className + '$' + info);
            var thisLookup = MethodHandles.privateLookupIn(thisClass, lookup);
            var thisClassPod = thisLookup.defineClass(data);

            // Bind $type
            bindTypeVariable(thisClassPod, info);

            return thisClassPod;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateGroupClass(ClassTypeInfo host, ClassDesc hostDesc, ClassGroupInfo group, ClassDesc groupDesc, ClassDesc groupImplDesc) {
        byte[] data = ClassFile.of().build(groupImplDesc, cb -> {
            cb.withInterfaceSymbols(groupDesc);
            cb.with(NestHostAttribute.of(hostDesc));
            cb.with(EnclosingMethodAttribute.of(hostDesc, Optional.empty(), Optional.empty()));
            cb.with(InnerClassesAttribute.of(InnerClassInfo.of(groupImplDesc, Optional.empty(), Optional.empty())));

            // Enclosing object
            cb.withField("this$0", hostDesc, ACC_FINAL | ACC_SYNTHETIC);

            // Constructor
            cb.withMethodBody(INIT_NAME, MethodTypeDesc.of(CD_void, hostDesc), 0, cob -> cob
                .aload(0)
                .aload(1)
                .putfield(groupImplDesc, "this$0", hostDesc)
                .aload(0)
                .invokespecial(CD_Object, INIT_NAME, MTD_void)
                .return_());

            // Attributes
            for (ClassAttrInfo attr : collectAttributes(host)) {
                if (attr.group().isPresent() && attr.group().get().equals(group.name())) {
                    buildAttr(cb, hostDesc, groupImplDesc, attr);
                }
            }
        });

        try {
            lookup.defineClass(data);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void bindTypeVariable(Class<?> clazz, ClassTypeInfo info) throws ReflectiveOperationException {
        var handle = lookup.findStaticVarHandle(clazz, "$type", StableValue.class);
        var holder = (StableValue<ClassTypeInfo>) handle.get();
        holder.setOrThrow(info);
    }

    private void buildAttr(ClassBuilder builder, ClassDesc desc, ClassAttrInfo attr) {
        if (attr.isSerialized() || !attr.isProperty()) {
            buildInstanceAttr(builder, desc, attr);
        } else {
            buildPropertyAttr(builder, attr);
        }
    }

    private void buildAttr(ClassBuilder builder, ClassDesc hostDesc, ClassDesc groupDesc, ClassAttrInfo attr) {
        if (attr.isSerialized() || !attr.isProperty()) {
            buildInstanceAttr(builder, hostDesc, groupDesc, attr);
        } else {
            buildPropertyAttr(builder, attr);
        }
    }

    private void buildInstanceAttr(ClassBuilder builder, ClassDesc desc, ClassAttrInfo attr) {
        var attrField = toFieldName(attr);
        var attrDesc = toClassDesc(attr.type(), true);

        builder.withMethodBody(toGetterName(attr), MethodTypeDesc.of(attrDesc), ClassFile.ACC_PUBLIC, cob -> cob
            .aload(0)
            .getfield(desc, attrField, attrDesc)
            .return_(TypeKind.from(attrDesc)));

        builder.withMethodBody(toSetterName(attr), MethodTypeDesc.of(CD_void, attrDesc), ClassFile.ACC_PUBLIC, cob -> cob
            .aload(0)
            .loadLocal(TypeKind.from(attrDesc), 1)
            .putfield(desc, attrField, attrDesc)
            .return_());
    }

    private void buildInstanceAttr(ClassBuilder builder, ClassDesc hostDesc, ClassDesc groupDesc, ClassAttrInfo attr) {
        var attrField = toFieldName(attr);
        var attrDesc = toClassDesc(attr.type(), true);

        builder.withMethodBody(toGetterName(attr), MethodTypeDesc.of(attrDesc), ClassFile.ACC_PUBLIC, cob -> cob
            .aload(0)
            .getfield(groupDesc, "this$0", hostDesc)
            .getfield(hostDesc, attrField, attrDesc)
            .return_(TypeKind.from(attrDesc)));

        builder.withMethodBody(toSetterName(attr), MethodTypeDesc.of(CD_void, attrDesc), ClassFile.ACC_PUBLIC, cob -> cob
            .aload(0)
            .getfield(groupDesc, "this$0", hostDesc)
            .loadLocal(TypeKind.from(attrDesc), 1)
            .putfield(hostDesc, attrField, attrDesc)
            .return_());
    }

    private void buildPropertyAttr(ClassBuilder builder, ClassAttrInfo attr) {
        var attrDesc = toClassDesc(attr.type(), true);

        builder.withMethod(toGetterName(attr), MethodTypeDesc.of(attrDesc), ClassFile.ACC_PUBLIC, mb -> mb
            .withCode(TypeRuntimeGenerator::buildPropertyAccessException));

        builder.withMethod(toSetterName(attr), MethodTypeDesc.of(CD_void, attrDesc), ClassFile.ACC_PUBLIC, mb -> mb
            .withCode(TypeRuntimeGenerator::buildPropertyAccessException));
    }

    private static void buildPropertyAccessException(CodeBuilder builder) {
        builder
            .new_(CD_UnsupportedOperationException)
            .dup()
            .ldc("attempt to access a non-serializable property attribute")
            .invokespecial(CD_UnsupportedOperationException, INIT_NAME, MethodTypeDesc.of(CD_void, CD_String))
            .athrow();
    }

    private ClassDesc toImplClassDesc(ClassTypeInfo info) {
        return toClassDesc(info).nested("POD");
    }

    private ClassDesc toClassDesc(ClassGroupInfo group) {
        return toClassDesc(group.host()).nested(toTypeName(group));
    }

    private ClassDesc toClassDesc(ClassTypeInfo info) {
        return toClassDesc().nested(info.name());
    }

    private ClassDesc toClassDesc(EnumTypeInfo info) {
        return toClassDesc().nested(info.name());
    }

    private ClassDesc toClassDesc() {
        return ClassDesc.of(packageName, className);
    }

    private ClassDesc toClassDesc(TypeInfo info, boolean useWrapperType) {
        return switch (info) {
            case ClassTypeInfo i -> toClassDesc(i);
            case EnumTypeInfo i -> useWrapperType ? CD_Value : toClassDesc(i);
            case AtomTypeInfo i -> getBuiltin(i.base().name()).flatMap(Class::describeConstable).orElseThrow();
            case ContainerTypeInfo i -> {
                var itemType = toClassDesc(i.itemType(), useWrapperType);
                if (itemType.isPrimitive()) {
                    yield itemType.arrayType();
                } else {
                    yield CD_List;
                }
            }
            case PointerTypeInfo _ -> CD_Ref;
        };
    }

    private String toFieldName(ClassAttrInfo attr) {
        return attr.group()
            .map(group -> group + "$" + attr.name())
            .orElseGet(attr::name);
    }

    private String toFieldName(ClassGroupInfo group) {
        return group.name();
    }
}
