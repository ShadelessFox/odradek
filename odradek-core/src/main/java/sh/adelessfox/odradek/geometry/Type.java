package sh.adelessfox.odradek.geometry;

public sealed interface Type {
    int components();

    boolean unsigned();

    boolean normalized();

    default Type withComponents(int components) {
        if (components() == components) {
            return this;
        }
        return switch (this) {
            case I8 x -> new I8(components, x.unsigned(), x.normalized());
            case I16 x -> new I16(components, x.unsigned(), x.normalized());
            case I32 x -> new I32(components, x.unsigned(), x.normalized());
            case F16 _ -> new F16(components);
            case F32 _ -> new F32(components);
            case X10Y10Z10W2 x -> new X10Y10Z10W2(components, x.unsigned(), x.normalized());
        };
    }

    record I8(int components, boolean unsigned, boolean normalized) implements Type {
    }

    record I16(int components, boolean unsigned, boolean normalized) implements Type {
    }

    record I32(int components, boolean unsigned, boolean normalized) implements Type {
    }

    record F16(int components) implements Type {
        @Override
        public boolean unsigned() {
            return false;
        }

        @Override
        public boolean normalized() {
            return false;
        }
    }

    record F32(int components) implements Type {
        @Override
        public boolean unsigned() {
            return false;
        }

        @Override
        public boolean normalized() {
            return false;
        }
    }

    record X10Y10Z10W2(int components, boolean unsigned, boolean normalized) implements Type {
    }
}
