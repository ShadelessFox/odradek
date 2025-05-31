package sh.adelessfox.odradek.rtti.generator;

import com.palantir.javapoet.ArrayTypeName;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import sh.adelessfox.odradek.rtti.data.Ref;
import sh.adelessfox.odradek.rtti.data.Value;
import sh.adelessfox.odradek.rtti.generator.data.*;

import javax.lang.model.SourceVersion;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

class TypeNameUtil {
    private static final Set<String> OBJECT_METHODS = Set.of(
        "hashCode",
        "equals",
        "clone",
        "notify",
        "notifyAll",
        "wait",
        "finalize"
    );

    private TypeNameUtil() {
    }

    static String getJavaConstantName(EnumTypeInfo info, EnumValueInfo value) {
        String name = value.name();
        if (!isValidIdentifier(name)) {
            name = "_" + name;
        }
        if (!isValidIdentifier(name)) {
            name = "_" + info.values().indexOf(value);
        }
        return name;
    }

    static String getJavaPropertyName(String name) {
        if (name.length() > 1 && name.matches("^m[A-Z].*$")) {
            name = name.substring(1);
        }
        if (name.indexOf(1, '_') > 0) {
            name = Arrays.stream(name.split("_"))
                .filter(part -> !part.isBlank())
                .map(TypeNameUtil::capitalize)
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

    static ClassName getTypeName(ClassTypeInfo type, TypeGenerator generator) {
        return (ClassName) getTypeName(type, generator, false);
    }

    static TypeName getTypeName(TypeInfo type, TypeGenerator generator) {
        return getTypeName(type, generator, false);
    }

    static TypeName getTypeName(TypeInfo type, TypeGenerator generator, boolean useWrapperType) {
        if (type instanceof EnumTypeInfo && useWrapperType) {
            return ParameterizedTypeName.get(ClassName.get(Value.class), getTypeName(type, generator, false));
        } else if (type instanceof ClassTypeInfo || type instanceof EnumTypeInfo) {
            return ClassName.get("" /* same package */, getJavaTypeName(type));
        } else if (type instanceof AtomTypeInfo(var name, var parent)) {
            if (parent.isPresent()) {
                return getTypeName(parent.get(), generator, false);
            }
            var builtin = generator.getBuiltin(name);
            if (builtin.isPresent()) {
                return builtin.get();
            }
            throw new IllegalArgumentException("Unknown atom type: " + name);
        } else if (type instanceof PointerTypeInfo pointer) {
            return ParameterizedTypeName.get(ClassName.get(Ref.class), getTypeName(pointer.type().value(), generator, false).box());
        } else if (type instanceof ContainerTypeInfo container) {
            TypeName argument = getTypeName(container.type().value(), generator, false);
            if (argument.isPrimitive()) {
                return ArrayTypeName.of(argument);
            } else {
                return ParameterizedTypeName.get(ClassName.get(List.class), argument);
            }
        }
        throw new IllegalArgumentException("Unknown type: " + type.typeName());
    }

    private static String getJavaTypeName(TypeInfo info) {
        return capitalize(info.typeName().fullName());
    }

    private static String capitalize(String name) {
        if (name.length() >= 2 && Character.isLowerCase(name.charAt(0)) && Character.isUpperCase(name.charAt(1))) {
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
        return name;
    }

    private static boolean isValidIdentifier(String name) {
        return SourceVersion.isIdentifier(name) && !SourceVersion.isKeyword(name);
    }
}
