package sh.adelessfox.odradek.rtti.generator;

import com.palantir.javapoet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.*;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataHolder;
import sh.adelessfox.odradek.rtti.data.Ref;
import sh.adelessfox.odradek.rtti.data.Value;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import sh.adelessfox.odradek.rtti.runtime.TypeGenerator;
import sh.adelessfox.odradek.rtti.runtime.TypedObject;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

final class TypeSourceGenerator extends TypeGenerator<TypeMirror> {
    private static final Logger log = LoggerFactory.getLogger(TypeSourceGenerator.class);

    private static final ClassName NAME_List = ClassName.get(List.class);
    private static final ClassName NAME_Ref = ClassName.get(Ref.class);
    private static final ClassName NAME_Value = ClassName.get(Value.class);

    private final Set<TypeInfo> types = new TreeSet<>(Comparator.comparing(TypeInfo::name));
    private final String packageName;
    private final String className;

    TypeSourceGenerator(String packageName, String className) {
        this.packageName = packageName;
        this.className = className;
    }

    void add(TypeInfo info) {
        types.add(info);
    }

    void addAll(Collection<? extends TypeInfo> info) {
        types.addAll(info);
    }

    JavaFile build() {
        log.debug("Generating interfaces");
        return JavaFile.builder(packageName, buildNamespace()).build();
    }

    private TypeSpec buildNamespace() {
        var builder = TypeSpec.interfaceBuilder(toTypeName())
            .addModifiers(Modifier.PUBLIC);

        for (TypeInfo info : types) {
            if (info instanceof ClassTypeInfo i) {
                builder.addType(buildClass(i));
            } else if (info instanceof EnumTypeInfo i) {
                builder.addType(buildEnum(i));
            }
        }

        return builder.build();
    }

    private TypeSpec buildClass(ClassTypeInfo info) {
        var builder = TypeSpec.interfaceBuilder(toTypeName(info))
            .addJavadoc("flags = $L, version = $L", info.flags(), info.version())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addSuperinterface(TypedObject.class);

        // Extension
        getExtension(info.name()).ifPresent(builder::addSuperinterface);

        // Callback
        if (info.messages().contains("MsgReadBinary")) {
            getCallback(info.name()).ifPresent(callback -> {
                var deserialize = MethodSpec.methodBuilder("deserialize")
                    .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                    .addAnnotation(Override.class)
                    .addParameter(BinaryReader.class, "reader")
                    .addParameter(TypeFactory.class, "factory")
                    .addException(IOException.class)
                    .addCode("new $T().deserialize(reader, factory, this);", callback);

                builder.addMethod(deserialize.build());
            });

            builder.addSuperinterface(ExtraBinaryDataHolder.class);
        }

        var attrs = info.attrs().stream()
            .collect(Collectors.groupingBy(ClassAttrInfo::group));

        var groups = collectGroups(info).stream()
            .filter(group -> group.host() == info)
            .toList();

        // Bases
        for (ClassBaseInfo base : info.bases()) {
            builder.addSuperinterface(toTypeName(base.type()));
        }

        // Group accessors
        for (ClassGroupInfo group : groups) {
            builder.addMethod(MethodSpec.methodBuilder(toGetterName(group))
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(toGroupName(info, group))
                .build());
        }

        // Group interfaces
        for (ClassGroupInfo group : groups) {
            var groupMethods = attrs.getOrDefault(Optional.of(group.name()), List.of()).stream()
                .map(this::buildAttr).flatMap(Collection::stream)
                .toList();
            var groupInterfaces = group.inherits().stream()
                .map(inheritee -> toGroupName(inheritee, group))
                .toList();
            builder.addType(TypeSpec.interfaceBuilder(toGroupName(info, group))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addSuperinterfaces(groupInterfaces)
                .addMethods(groupMethods)
                .build());
        }

        // Direct attribute accessors
        for (ClassAttrInfo attr : attrs.getOrDefault(Optional.<String>empty(), List.of())) {
            builder.addMethods(buildAttr(attr));
        }

        return builder.build();
    }

    private List<MethodSpec> buildAttr(ClassAttrInfo attr) {
        var getter = MethodSpec.methodBuilder(toGetterName(attr))
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(toJavaType(attr.type()));

        var setter = MethodSpec.methodBuilder(toSetterName(attr))
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(toJavaType(attr.type()), "value");

        // Add the comment, if present
        attr.comment().ifPresent(getter::addJavadoc);

        if (!attr.isSerialized() && attr.isProperty()) {
            getter.addJavadoc("@deprecated This is a <b>non-serializable property</b> attribute. Retrieving its value will result in an exception.");
            setter.addJavadoc("@deprecated This is a <b>non-serializable property</b> attribute. Updating its value will result in an exception.");
        }

        return List.of(
            getter.build(),
            setter.build()
        );
    }

    private TypeSpec buildEnum(EnumTypeInfo info) {
        var type = info instanceof EnumSetTypeInfo ? Value.OfEnumSet.class : Value.OfEnum.class;
        var size = switch (info.size()) {
            case 1 -> TypeName.BYTE;
            case 2 -> TypeName.SHORT;
            case 4 -> TypeName.INT;
            default -> throw new IllegalStateException("Unexpected enum size: " + info.size());
        };

        var name = toTypeName(info);
        var builder = TypeSpec.enumBuilder(name)
            .addJavadoc("size = $L", info.size())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addSuperinterface(ParameterizedTypeName.get(ClassName.get(type), name))
            .addField(String.class, "name", Modifier.PRIVATE, Modifier.FINAL)
            .addField(size, "value", Modifier.PRIVATE, Modifier.FINAL)
            .addMethod(MethodSpec.constructorBuilder()
                .addParameter(String.class, "name")
                .addParameter(int.class, "value")
                .addCode("this.name = name;\nthis.value = ($T) value;", size)
                .build())
            .addMethod(MethodSpec.methodBuilder("value")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(int.class)
                .addStatement("return $L", "value")
                .build())
            .addMethod(MethodSpec.methodBuilder("toString")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("return $L", "name")
                .build());

        if (!(info instanceof EnumSetTypeInfo)) {
            builder
                .alwaysQualify(Value.class.getSimpleName())
                .avoidClashesWithNestedClasses(Value.class)
                .addMethod(MethodSpec.methodBuilder("valueOf")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(size, "value")
                    .returns(name)
                    .addStatement("return ($T) $T.valueOf($T.class, value)", name, Value.class, name)
                    .build());
        }

        for (EnumValueInfo value : info.values()) {
            builder.addEnumConstant(
                toJavaConstant(info, value),
                TypeSpec.anonymousClassBuilder("$S, $L", value.name(), value.value()).build()
            );
        }

        // Extension
        getExtension(info.name()).ifPresent(builder::addSuperinterface);

        return builder.build();
    }

    private ClassName toGroupName(TypeInfo info, ClassGroupInfo group) {
        return toTypeName(info).nestedClass(toTypeName(group));
    }

    private ClassName toTypeName(TypeInfo info) {
        return toTypeName().nestedClass(info.name());
    }

    private ClassName toTypeName() {
        return ClassName.get(packageName, className);
    }

    private TypeName toJavaType(TypeInfo info) {
        return switch (info) {
            case ClassTypeInfo i -> toTypeName(i);
            case EnumTypeInfo i -> {
                var name = toTypeName(i);
                yield ParameterizedTypeName.get(NAME_Value, name);
            }
            case AtomTypeInfo i -> getBuiltin(i.base().name()).map(TypeName::get).orElseThrow();
            case ContainerTypeInfo i -> {
                var itemType = toJavaType(i.itemType());
                if (itemType.isPrimitive()) {
                    yield ArrayTypeName.of(itemType);
                } else {
                    yield ParameterizedTypeName.get(NAME_List, itemType);
                }
            }
            case PointerTypeInfo i -> {
                var name = toJavaType(i.itemType()).box();
                yield ParameterizedTypeName.get(NAME_Ref, name);
            }
        };
    }

    static String toJavaConstant(EnumTypeInfo info, EnumValueInfo value) {
        String name = value.name();
        if (!SourceVersion.isName(name)) {
            name = "_" + name;
        }
        if (!SourceVersion.isName(name)) {
            name = "_" + info.values().indexOf(value);
        }
        return name;
    }
}
