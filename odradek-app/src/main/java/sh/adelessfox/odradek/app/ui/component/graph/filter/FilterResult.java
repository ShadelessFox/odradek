package sh.adelessfox.odradek.app.ui.component.graph.filter;

public enum FilterResult {
    PASS,
    FAIL,
    NOT_APPLICABLE;

    static FilterResult of(boolean value) {
        return value ? PASS : FAIL;
    }

    FilterResult negate() {
        return switch (this) {
            case PASS -> FAIL;
            case FAIL -> PASS;
            case NOT_APPLICABLE -> NOT_APPLICABLE;
        };
    }
}
