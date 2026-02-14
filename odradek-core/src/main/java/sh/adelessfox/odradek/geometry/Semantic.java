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
    Semantic JOINTS = new Joints();
    Semantic WEIGHTS = new Weights();

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

    record Joints() implements Semantic {
    }

    record Weights() implements Semantic {
    }
}
