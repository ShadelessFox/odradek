package sh.adelessfox.odradek.opengl;

import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;

public sealed interface GLCapability {
    OfBoolean DEPTH_TEST = new OfBoolean(GL_DEPTH_TEST);

    int id();

    Restorable save();

    record OfBoolean(int id) implements GLCapability {
        public boolean get() {
            return GL11.glGetBoolean(id);
        }

        public void set(boolean value) {
            if (value) {
                GL11.glEnable(id);
            } else {
                GL11.glDisable(id);
            }
        }

        @Override
        public Restorable save() {
            boolean value = get();
            return () -> set(value);
        }
    }

    @FunctionalInterface
    interface Restorable {
        void restore();
    }
}
