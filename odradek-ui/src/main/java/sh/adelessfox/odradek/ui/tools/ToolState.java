package sh.adelessfox.odradek.ui.tools;

import java.util.List;
import java.util.OptionalInt;

public record ToolState(
    Anchor left,
    Anchor right,
    Anchor bottom,
    boolean showToolNames
) {
    public record Anchor(
        Group primary,
        Group secondary,
        Weights weights
    ) {
        public record Group(List<String> tools, OptionalInt selection) {
            public Group {
                tools = List.copyOf(tools);
            }
        }

        public record Weights(double inner, double outer) {
        }
    }
}
