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
     * It must follow the format {@code "id,order"}, where {@code id} is a unique identifier
     * of the group within {@link #parent()}, and {@code order} is an integer that denotes the
     * order of the group within the parent action.
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
