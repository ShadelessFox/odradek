package sh.adelessfox.odradek.rtti.generator;

import com.palantir.javapoet.*;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.*;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataHolder;
import sh.adelessfox.odradek.rtti.data.Value;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import sh.adelessfox.odradek.rtti.generator.data.*;
import sh.adelessfox.odradek.rtti.runtime.TypedObject;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class TypeGenerator {
    private static final String NO_CATEGORY = "";
    private static final AnnotationSpec extensionAnnotation = AnnotationSpec.builder(Extension.class).build();

    private final Map<String, TypeName> callbacks = new HashMap<>();
    private final Map<String, TypeName> builtins = new HashMap<>();
    private final Map<String, TypeName> extensions = new HashMap<>();

    void addCallback(String targetType, TypeName handlerType) {
        if (callbacks.containsKey(targetType)) {
            throw new IllegalArgumentException("Callback for type '" + targetType + "' already exists");
        }
        callbacks.put(targetType, handlerType);
    }

    void addBuiltin(String typeName, TypeName javaType) {
        if (builtins.containsKey(typeName)) {
            throw new IllegalArgumentException("Builtin for type '" + typeName + "' already exists");
        }
        builtins.put(typeName, javaType);
    }

    void addExtension(String typeName, TypeName javaType) {
        if (extensions.containsKey(typeName)) {
            throw new IllegalArgumentException("Extension for type '" + typeName + "' already exists");
        }
        extensions.put(typeName, javaType);
    }

    Optional<TypeSpec> generate(TypeInfo type) {
        return switch (type) {
            case ClassTypeInfo t -> Optional.of(generateClass(t));
            case EnumTypeInfo t -> Optional.of(generateEnum(t));
            case AtomTypeInfo ignored -> Optional.empty();
            case ContainerTypeInfo ignored -> Optional.empty();
            case PointerTypeInfo ignored -> Optional.empty();
        };
    }

    Optional<TypeName> getBuiltin(String name) {
        return Optional.ofNullable(builtins.get(name));
    }

    private TypeSpec generateClass(ClassTypeInfo info) {
        var categories = collectCategories(info);
        var root = categories.remove(NO_CATEGORY);

        var name = TypeNameUtil.getTypeName(info, this);
        var builder = TypeSpec.interfaceBuilder(name)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addSuperinterfaces(info.bases().stream().map(this::generateBase).toList())
            .addSuperinterface(TypedObject.class)
            .addAnnotation(AnnotationSpec.builder(Serializable.class)
                .addMember("version", "$L", info.version())
                .addMember("flags", "$L", info.flags())
                .build())
            .addTypes(categories.values().stream().map(this::generateCategory).toList())
            .addMethods(root != null ? generateAttrs(root.attrs) : List.of())
            .addMethods(categories.values().stream().map(this::generateCategoryAttr).toList());

        if (info.messages().contains("MsgReadBinary")) {
            var callback = callbacks.get(info.name());
            if (callback != null) {
                var deserialize = MethodSpec.methodBuilder("deserialize")
                    .addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
                    .addAnnotation(Override.class)
                    .addParameter(BinaryReader.class, "reader")
                    .addParameter(TypeFactory.class, "factory")
                    .addException(IOException.class)
                    .addCode("new $T().deserialize(reader, factory, this);", callback);

                builder.addMethod(deserialize.build());
            }

            builder.addSuperinterface(ExtraBinaryDataHolder.class);
        }

        var extension = extensions.get(info.name());
        if (extension != null) {
            builder.addSuperinterface(extension.annotated(extensionAnnotation));
        }

        return builder.build();
    }

    private TypeSpec generateCategory(CategoryInfo category) {
        return TypeSpec.interfaceBuilder(category.name + "Category")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addAnnotation(Serializable.class)
            .addSuperinterfaces(category.bases.stream()
                .map(x -> TypeNameUtil.getTypeName(x, this).nestedClass(category.javaTypeName()))
                .toList())
            .addMethods(generateAttrs(category.attrs))
            .build();
    }

    private List<MethodSpec> generateAttrs(List<ClassAttrInfo> attrs) {
        List<MethodSpec> methods = new ArrayList<>(attrs.size());
        for (ClassAttrInfo attr : attrs) {
            // NOTE: Consider skipping save-state attributes here
            methods.add(generateGetterAttr(attr));
            methods.add(generateSetterAttr(attr));
        }
        return methods;
    }

    private MethodSpec generateCategoryAttr(CategoryInfo category) {
        var builder = MethodSpec.methodBuilder(TypeNameUtil.getJavaPropertyName(category.name))
            .addAnnotation(AnnotationSpec.builder(Category.class)
                .addMember("name", "$S", category.name)
                .build())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(ClassName.get("" /* same package */, category.javaTypeName()));
        if (!category.bases.isEmpty()) {
            builder.addAnnotation(Override.class);
        }
        return builder.build();
    }

    private MethodSpec generateGetterAttr(ClassAttrInfo attr) {
        var builder = AnnotationSpec.builder(Attr.class)
            .addMember("name", "$S", attr.name())
            .addMember("type", "$S", attr.type().typeName())
            .addMember("position", "$L", attr.position())
            .addMember("offset", "$L", attr.offset());
        if (attr.flags() != 0) {
            builder.addMember("flags", "$L", attr.flags());
        }
        attr.min().ifPresent(min -> builder.addMember("min", "$S", min));
        attr.max().ifPresent(max -> builder.addMember("max", "$S", max));
        return MethodSpec
            .methodBuilder(TypeNameUtil.getJavaPropertyName(attr.name()))
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addAnnotation(builder.build())
            .returns(TypeNameUtil.getTypeName(attr.type().value(), this, true))
            .build();
    }

    private MethodSpec generateSetterAttr(ClassAttrInfo attr) {
        return MethodSpec
            .methodBuilder(TypeNameUtil.getJavaPropertyName(attr.name()))
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addParameter(TypeNameUtil.getTypeName(attr.type().value(), this, true), "value")
            .returns(TypeName.VOID)
            .build();
    }

    private TypeName generateBase(ClassBaseInfo base) {
        return TypeNameUtil.getTypeName(base.type(), this)
            .annotated(AnnotationSpec.builder(Base.class)
                .addMember("offset", "$L", base.offset())
                .build());
    }

    private TypeSpec generateEnum(EnumTypeInfo info) {
        var name = (ClassName) TypeNameUtil.getTypeName(info, this);
        var builder = TypeSpec.enumBuilder(name)
            .addSuperinterface(ParameterizedTypeName.get(ClassName.get(info.flags() ? Value.OfEnumSet.class : Value.OfEnum.class), name))
            .addAnnotation(AnnotationSpec.builder(Serializable.class)
                .addMember("size", "$L", info.size().bytes())
                .build())
            .addField(String.class, "name", Modifier.PRIVATE, Modifier.FINAL)
            .addField(info.size().type(), "value", Modifier.PRIVATE, Modifier.FINAL)
            .addMethod(MethodSpec.constructorBuilder()
                .addParameter(String.class, "name")
                .addParameter(int.class, "value")
                .addCode("this.name = name;\nthis.value = ($T) value;", info.size().type())
                .build());
        if (!info.flags()) {
            builder.addMethod(MethodSpec.methodBuilder("valueOf")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(info.size().type(), "value")
                .returns(name)
                .addStatement("return ($T) $T.valueOf($T.class, $L)", name, Value.class, name, "value")
                .build());
            builder.alwaysQualify(Value.class.getSimpleName());
            builder.avoidClashesWithNestedClasses(Value.class);
        }
        builder.addMethod(MethodSpec.methodBuilder("value")
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
                .build())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        for (EnumValueInfo value : info.values()) {
            builder.addEnumConstant(
                TypeNameUtil.getJavaConstantName(info, value),
                TypeSpec.anonymousClassBuilder("$S, $L", value.name(), value.value()).build()
            );
        }
        return builder.build();
    }

    private static Map<String, CategoryInfo> collectCategories(ClassTypeInfo info) {
        var inheritedCategories = collectAllCategories(info).stream()
            .filter(category -> findCategoryHost(info, category).orElse(null) == info)
            .map(category -> Map.entry(category, findInheritedCategories(info, category)))
            .filter(pair -> !pair.getValue().isEmpty())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        var declaredCategories = info.attrs().stream()
            .collect(Collectors.groupingBy(
                attr -> attr.category().orElse(NO_CATEGORY),
                LinkedHashMap::new,
                Collectors.toList()
            ));
        return Stream.concat(inheritedCategories.keySet().stream(), declaredCategories.keySet().stream())
            .distinct()
            .collect(Collectors.toMap(
                Function.identity(),
                category -> new CategoryInfo(
                    category,
                    declaredCategories.getOrDefault(category, List.of()),
                    inheritedCategories.getOrDefault(category, List.of())
                ),
                (u, v) -> {
                    throw new IllegalStateException("Duplicate category: " + u.name);
                },
                LinkedHashMap::new
            ));
    }

    private static Set<String> collectAllCategories(ClassTypeInfo info) {
        var categories = new HashSet<String>();
        collectAllCategories(info, categories);
        return categories;
    }

    private static void collectAllCategories(ClassTypeInfo info, Set<String> categories) {
        for (ClassAttrInfo attr : info.attrs()) {
            attr.category().ifPresent(categories::add);
        }
        for (ClassBaseInfo base : info.bases()) {
            collectAllCategories(base.type(), categories);
        }
    }

    private static List<ClassTypeInfo> findInheritedCategories(ClassTypeInfo info, String name) {
        return info.bases().stream()
            .map(base -> findCategoryHost(base.type(), name))
            .flatMap(Optional::stream)
            .toList();
    }

    private static Optional<ClassTypeInfo> findCategoryHost(ClassTypeInfo info, String name) {
        for (ClassAttrInfo attr : info.attrs()) {
            if (attr.category().isPresent() && attr.category().get().equals(name)) {
                return Optional.of(info);
            }
        }
        var matches = info.bases().stream()
            .map(base -> findCategoryHost(base.type(), name))
            .flatMap(Optional::stream)
            .limit(2)
            .toList();
        return switch (matches.size()) {
            case 0 -> Optional.empty();
            case 1 -> Optional.of(matches.getFirst());
            // In cases where multiple bases have the same category interface,
            // this type must implement, a new interface that inherits from all
            // of them to make the Java compiler happy
            default -> Optional.of(info);
        };
    }

    private record CategoryInfo(
        String name,
        List<ClassAttrInfo> attrs,
        List<ClassTypeInfo> bases
    ) {
        private String javaTypeName() {
            return name + "Category";
        }
    }
}
