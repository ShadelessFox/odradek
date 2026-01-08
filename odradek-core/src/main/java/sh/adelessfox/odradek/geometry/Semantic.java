package sh.adelessfox.odradek.geometry;

public sealed interface Semantic {
    Semantic POSITION = new Position();
    Semantic NORMAL = new Normal();
    Semantic TANGENT_BFLIP = new TangentBFlip();
    Semantic TANGENT = new Tangent();
    Semantic BINORMAL = new Binormal();
    Semantic TEXTURE_0 = new Texture(0);
    Semantic TEXTURE_1 = new Texture(1);
    Semantic TEXTURE_2 = new Texture(2);
    Semantic COLOR = new Color();
    Semantic JOINTS_0 = new Joints(0);
    Semantic JOINTS_1 = new Joints(1);
    Semantic JOINTS_2 = new Joints(2);
    Semantic WEIGHTS_0 = new Weights(0);
    Semantic WEIGHTS_1 = new Weights(1);
    Semantic WEIGHTS_2 = new Weights(2);

    record Position() implements Semantic {
    }

    record Normal() implements Semantic {
    }

    record Tangent() implements Semantic {
    }

    record TangentBFlip() implements Semantic {
    }

    record Binormal() implements Semantic {
    }

    record Texture(int n) implements Semantic {
        public Texture {
            if (n < 0) {
                throw new IllegalArgumentException("n must be positive");
            }
        }
    }

    record Color() implements Semantic {
    }

    record Joints(int n) implements Semantic {
        public Joints {
            if (n < 0) {
                throw new IllegalArgumentException("n must be positive");
            }
        }
    }

    record Weights(int n) implements Semantic {
        public Weights {
            if (n < 0) {
                throw new IllegalArgumentException("n must be positive");
            }
        }
    }
}
