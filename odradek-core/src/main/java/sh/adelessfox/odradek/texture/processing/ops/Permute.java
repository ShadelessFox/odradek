package sh.adelessfox.odradek.texture.processing.ops;

import sh.adelessfox.odradek.texture.Channel;
import sh.adelessfox.odradek.texture.processing.TileContext;
import wtf.reversed.toolbox.collect.Floats;

public final class Permute implements Operation {
    private final Source r;
    private final Source g;
    private final Source b;
    private final Source a;

    private Permute(Source r, Source g, Source b, Source a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    @Override
    public void process(TileContext ctx, Floats.Mutable buf) {
        writeChannel(r, ctx, buf, 0);
        writeChannel(g, ctx, buf, 1);
        writeChannel(b, ctx, buf, 2);
        writeChannel(a, ctx, buf, 3);
    }

    private void writeChannel(Source src, TileContext ctx, Floats.Mutable buf, int channel) {
        switch (src) {
            case Source.Const c -> writeConst(ctx, c, buf, channel);
            case Source.From f -> writeFrom(ctx, f, buf, channel);
        }
    }

    private static void writeConst(TileContext ctx, Source.Const src, Floats.Mutable dst, int dstIndex) {
        var value = src.value();
        for (int i = dstIndex, len = ctx.pixelCount() * 4; i < len; i += 4) {
            dst.set(i, value);
        }
    }

    private static void writeFrom(TileContext ctx, Source.From src, Floats.Mutable dst, int dstIndex) {
        var srcIndex = src.channel().index();
        if (srcIndex == dstIndex) {
            return;
        }
        for (int i = 0, len = ctx.pixelCount() * 4; i < len; i += 4) {
            dst.set(i + dstIndex, dst.get(i + srcIndex));
        }
    }

    private sealed interface Source {
        record Const(float value) implements Source {
        }

        record From(Channel channel) implements Source {
        }
    }

    public static final class Builder {
        private Source r = new Source.Const(0.0f);
        private Source g = new Source.Const(0.0f);
        private Source b = new Source.Const(0.0f);
        private Source a = new Source.Const(1.0f);

        Builder() {
        }

        public Builder channel(Channel dst, float value) {
            switch (dst) {
                case R -> r = new Source.Const(value);
                case G -> g = new Source.Const(value);
                case B -> b = new Source.Const(value);
                case A -> a = new Source.Const(value);
            }
            return this;
        }

        public Builder channel(Channel dst, Channel src) {
            switch (dst) {
                case R -> r = new Source.From(src);
                case G -> g = new Source.From(src);
                case B -> b = new Source.From(src);
                case A -> a = new Source.From(src);
            }
            return this;
        }

        public Permute build() {
            return new Permute(r, g, b, a);
        }
    }
}
