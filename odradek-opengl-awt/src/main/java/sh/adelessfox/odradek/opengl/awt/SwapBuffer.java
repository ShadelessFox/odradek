package sh.adelessfox.odradek.opengl.awt;

import sh.adelessfox.odradek.opengl.Framebuffer;
import sh.adelessfox.odradek.ui.Disposable;

import java.awt.*;
import java.util.Optional;
import java.util.function.Consumer;

abstract class SwapBuffer implements Disposable {
    abstract void present(Consumer<Framebuffer> renderer);

    abstract void resize(int width, int height);

    abstract Optional<Image> getImage();
}
