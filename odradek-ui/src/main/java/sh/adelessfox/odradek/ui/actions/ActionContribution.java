package sh.adelessfox.odradek.ui.actions;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ActionContributions.class)
public @interface ActionContribution {
    /**
     * An identifier of the parent action.
     */
    String parent();

    /**
     * A group this action contributes to.
     * <p>
     * It must follow the format {@code "order,id"}, where {@code order} is an integer that
     * denotes the order of the group within {@link #parent()}, and {@code id} is a unique
     * identifier of the group within {@link #parent()} used to group actions contributed
     * to the same group together.
     * <p>
     * Grouped actions are visually combined into one group, separated from other actions
     * using separators.
     */
    String group() default "1000,General";

    /**
     * A number representing the order of this action within the specified {@link #group()}.
     */
    int order() default Integer.MAX_VALUE;
}
