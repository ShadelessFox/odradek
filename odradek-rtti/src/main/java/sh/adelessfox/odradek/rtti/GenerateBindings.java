package sh.adelessfox.odradek.rtti;

import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.MODULE)
public @interface GenerateBindings {
    @interface Input {
        /**
         * Path to the file containing type definitions ({@code .json})
         */
        String types();

        /**
         * Path to the file containing extensions for types ({@code .json})
         */
        String extensions();
    }

    @interface Builtin {
        /**
         * Name of the type
         */
        String type();

        /**
         * Class that represents the type at runtime
         */
        Class<?> repr();
    }

    @interface Callback {
        /**
         * Name of the type for which the handler is registered
         */
        String type();

        /**
         * Handler class
         */
        Class<? extends ExtraBinaryDataCallback<?>> handler();
    }

    @interface Extension {
        /**
         * Name of the type to extend
         */
        String type();

        /**
         * Extension class
         */
        Class<?> extension();
    }

    /**
     * An input data
     */
    Input input();

    /**
     * A fully-qualified name of an interface under which the generated bindings will be placed
     */
    String target();

    /**
     * A collection of builtin types, such as numerics, strings, etc.
     */
    Builtin[] builtins() default {};

    /**
     * A collection of {@code MsgReadBinary} handlers.
     */
    Callback[] callbacks() default {};

    /**
     * A collection of {@code Extension}s that extend types with additional functionality via default interface methods.
     */
    Extension[] extensions() default {};
}
