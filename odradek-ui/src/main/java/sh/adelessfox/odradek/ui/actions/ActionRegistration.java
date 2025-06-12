package sh.adelessfox.odradek.ui.actions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionRegistration {
    /**
     * A unique identifier of this action.
     * <p>
     * May be empty, in such case the fully qualified class name of the action will be used.
     */
    String id() default "";

    /**
     * Display name of this action.
     * <p>
     * May contain a single mnemonic character denoted by the {@code &} symbol: {@code E&xit} will be rendered
     * as E<u>x</u>it and, will allow the user to activate this action using the {@code Alt+X} keyboard shortcut
     * while within its context.
     */
    String name();

    /**
     * Description of this action.
     */
    String description() default "";
}
