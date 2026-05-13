package sh.adelessfox.odradek.texture.processing.ops;

import sh.adelessfox.odradek.texture.processing.TileContext;
import wtf.reversed.toolbox.collect.Floats;

import java.util.function.Consumer;

public sealed interface Operation permits Permute {
    void process(TileContext ctx, Floats.Mutable buf);

    static Operation permute(Consumer<Permute.Builder> consumer) {
        var builder = new Permute.Builder();
        consumer.accept(builder);
        return builder.build();
    }
}
