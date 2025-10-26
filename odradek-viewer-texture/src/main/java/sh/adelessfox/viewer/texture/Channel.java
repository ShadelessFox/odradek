package sh.adelessfox.viewer.texture;

public enum Channel {
    R("Red") {
        @Override
        public int getComponent(int rgb) {
            return rgb >> 16 & 0xff;
        }

        @Override
        public int setComponent(int rgb, int value) {
            return rgb & 0xff00ffff | value << 16;
        }
    },
    G("Green") {
        @Override
        public int getComponent(int rgb) {
            return rgb >> 8 & 0xff;
        }

        @Override
        public int setComponent(int rgb, int value) {
            return rgb & 0xffff00ff | value << 8;
        }
    },
    B("Blue") {
        @Override
        public int getComponent(int rgb) {
            return rgb & 0xff;
        }

        @Override
        public int setComponent(int rgb, int value) {
            return rgb & 0xffffff00 | value;
        }
    },
    A("Alpha") {
        @Override
        public int getComponent(int rgb) {
            return rgb >> 24 & 0xff;
        }

        @Override
        public int setComponent(int rgb, int value) {
            return rgb & 0x00ffffff | value << 24;
        }
    };

    private final String name;

    Channel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract int getComponent(int rgb);

    public abstract int setComponent(int rgb, int value);
}
