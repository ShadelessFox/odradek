package sh.adelessfox.odradek.export.png.format;

public sealed interface PngColorType {
    TrueColor RGBA8 = new TrueColor(true, 8);

    record Greyscale(boolean alpha, int bitDepth) implements PngColorType {
        public Greyscale {
            if (bitDepth != 8 && bitDepth != 16) {
                throw new IllegalArgumentException("Bit depth must be 8 or 16 for greyscale color type");
            }
        }
    }

    record TrueColor(boolean alpha, int bitDepth) implements PngColorType {
        public TrueColor {
            if (bitDepth != 8 && bitDepth != 16) {
                throw new IllegalArgumentException("Bit depth must be 8 or 16 for true color type");
            }
        }
    }

    int bitDepth();

    default int channels() {
        return switch (this) {
            case Greyscale(boolean alpha, _) -> alpha ? 2 : 1;
            case TrueColor(boolean alpha, _) -> alpha ? 4 : 3;
        };
    }
}
