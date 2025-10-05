package sh.adelessfox.odradek.rtti.runtime;

import sh.adelessfox.odradek.rtti.*;
import sh.adelessfox.odradek.rtti.data.Ref;
import sh.adelessfox.odradek.rtti.data.Value;

import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.TypeKind;
import java.lang.classfile.attribute.*;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.AccessFlag;
import java.util.*;
import java.util.function.Consumer;

public final class TypeRuntimeGenerator extends TypeGenerator<Class<?>> {
    private static final ClassDesc CD_Ref = Ref.class.describeConstable().orElseThrow();
    private static final ClassDesc CD_Value = Value.class.describeConstable().orElseThrow();
    private static final ClassDesc CD_List = List.class.describeConstable().orElseThrow();
    private static final ClassDesc CD_StableValue = StableValue.class.describeConstable().orElseThrow();
    private static final ClassDesc CD_ClassTypeInfo = ClassTypeInfo.class.describeConstable().orElseThrow();

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

            List<Consumer<CodeBuilder>> constructor = new ArrayList<>(1);
            int nestedClassIndex = 1;

            // Type
            cb.withField("$type", CD_StableValue, ClassFile.ACC_STATIC | ClassFile.ACC_FINAL | ClassFile.ACC_SYNTHETIC);
            cb.withMethod(ConstantDescs.CLASS_INIT_NAME, ConstantDescs.MTD_void, ClassFile.ACC_STATIC, mb -> mb
                .withCode(cob -> cob
                    .invokestatic(CD_StableValue, "of", MethodTypeDesc.of(CD_StableValue), true)
                    .putstatic(desc, "$type", CD_StableValue)
                    .return_()));
            cb.withMethod("getType", MethodTypeDesc.of(CD_ClassTypeInfo), ClassFile.ACC_PUBLIC, mb -> mb
                .withCode(cob -> cob
                    .getstatic(desc, "$type", CD_StableValue)
                    .invokeinterface(CD_StableValue, "orElseThrow", MethodTypeDesc.of(ConstantDescs.CD_Object))
                    .checkcast(CD_ClassTypeInfo)
                    .areturn()));

            // Groups
            for (ClassGroupInfo group : collectGroups(info)) {
                var groupField = toFieldName(group);
                var groupDesc = toClassDesc(group);

                // Field
                cb.withField(groupField, groupDesc, ClassFile.ACC_FINAL | ClassFile.ACC_SYNTHETIC);

                // Getter
                cb.withMethod(toGetterName(group), MethodTypeDesc.of(groupDesc), ClassFile.ACC_PUBLIC, mb -> mb
                    .withCode(cob -> cob
                        .aload(0)
                        .getfield(desc, groupField, groupDesc)
                        .areturn()));

                var groupImplDesc = desc.nested(String.valueOf(nestedClassIndex++));
                cb.with(NestMembersAttribute.ofSymbols(groupImplDesc));
                cb.with(InnerClassesAttribute.of(InnerClassInfo.of(groupImplDesc, Optional.empty(), Optional.empty())));

                // Constructor initializer
                constructor.add(cob -> cob
                    .aload(0)
                    .new_(groupImplDesc)
                    .dup()
                    .aload(0)
                    .invokespecial(groupImplDesc, ConstantDescs.INIT_NAME, MethodTypeDesc.of(ConstantDescs.CD_void, desc))
                    .putfield(desc, groupField, groupDesc));

                // Class
                generateGroupClass(info, desc, group, groupDesc, groupImplDesc);
            }

            // Attributes
            for (ClassAttrInfo attr : collectAttributes(info)) {
                var attrField = toFieldName(attr);
                var attrDesc = toClassDesc(attr.type(), true);

                // Field
                cb.withField(attrField, attrDesc, ClassFile.ACC_PRIVATE);

                // Getter and setter, if no group
                if (attr.group().isEmpty()) {
                    cb.withMethod(toGetterName(attr), MethodTypeDesc.of(attrDesc), ClassFile.ACC_PUBLIC, mb -> mb
                        .withCode(cob -> cob
                            .aload(0)
                            .getfield(desc, attrField, attrDesc)
                            .return_(TypeKind.from(attrDesc))));
                    cb.withMethod(toSetterName(attr), MethodTypeDesc.of(ConstantDescs.CD_void, attrDesc), ClassFile.ACC_PUBLIC, mb -> mb
                        .withCode(cob -> cob
                            .aload(0)
                            .loadLocal(TypeKind.from(attrDesc), 1)
                            .putfield(desc, attrField, attrDesc)
                            .return_()));
                }
            }

            // Constructor
            cb.withMethod(ConstantDescs.INIT_NAME, ConstantDescs.MTD_void, ClassFile.ACC_PUBLIC, mb -> mb
                .withCode(cob -> {
                    cob.aload(0);
                    cob.invokespecial(ConstantDescs.CD_Object, ConstantDescs.INIT_NAME, ConstantDescs.MTD_void);

                    constructor.forEach(c -> c.accept(cob));

                    cob.return_();
                }));
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

    @SuppressWarnings("unchecked")
    private void bindTypeVariable(Class<?> clazz, ClassTypeInfo info) throws ReflectiveOperationException {
        var handle = lookup.findStaticVarHandle(clazz, "$type", StableValue.class);
        var holder = (StableValue<ClassTypeInfo>) handle.get();
        holder.setOrThrow(info);
    }

    private void generateGroupClass(ClassTypeInfo host, ClassDesc hostDesc, ClassGroupInfo group, ClassDesc groupDesc, ClassDesc groupImplDesc) {
        byte[] data = ClassFile.of().build(groupImplDesc, cb -> {
            cb.withInterfaceSymbols(groupDesc);
            cb.with(NestHostAttribute.of(hostDesc));
            cb.with(EnclosingMethodAttribute.of(hostDesc, Optional.empty(), Optional.empty()));
            cb.with(InnerClassesAttribute.of(InnerClassInfo.of(groupImplDesc, Optional.empty(), Optional.empty())));

            // Enclosing object
            cb.withField("this$0", hostDesc, ClassFile.ACC_FINAL | ClassFile.ACC_SYNTHETIC);

            // Constructor
            cb.withMethod(ConstantDescs.INIT_NAME, MethodTypeDesc.of(ConstantDescs.CD_void, hostDesc), 0, mb -> mb
                .withCode(cob -> cob
                    .aload(0)
                    .aload(1)
                    .putfield(groupImplDesc, "this$0", hostDesc)
                    .aload(0)
                    .invokespecial(ConstantDescs.CD_Object, ConstantDescs.INIT_NAME, ConstantDescs.MTD_void)
                    .return_()));

            // Attributes
            for (ClassAttrInfo attr : collectAttributes(host)) {
                if (attr.group().isEmpty() || !attr.group().get().equals(group.name())) {
                    continue;
                }

                var attrField = toFieldName(attr);
                var attrDesc = toClassDesc(attr.type(), true);

                // Getter
                cb.withMethod(toGetterName(attr), MethodTypeDesc.of(attrDesc), ClassFile.ACC_PUBLIC, mb -> mb
                    .withCode(cob -> cob
                        .aload(0)
                        .getfield(groupImplDesc, "this$0", hostDesc)
                        .getfield(hostDesc, attrField, attrDesc)
                        .return_(TypeKind.from(attrDesc))));

                // Setter
                cb.withMethod(toGetterName(attr), MethodTypeDesc.of(ConstantDescs.CD_void, attrDesc), ClassFile.ACC_PUBLIC, mb -> mb
                    .withCode(cob -> cob
                        .aload(0)
                        .getfield(groupImplDesc, "this$0", hostDesc)
                        .loadLocal(TypeKind.from(attrDesc), 1)
                        .putfield(hostDesc, attrField, attrDesc)
                        .return_()));
            }
        });

        try {
            lookup.defineClass(data);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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
            case AtomTypeInfo i -> {
                if (i.base().isPresent()) {
                    i = i.base().get();
                }
                yield getBuiltin(i.name()).flatMap(Class::describeConstable).orElseThrow();
            }
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
