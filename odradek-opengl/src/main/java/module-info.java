module odradek.opengl {
    requires static java.desktop;

    requires odradek.core;
    requires org.lwjgl.natives;
    requires org.lwjgl.opengl.natives;
    requires transitive org.lwjgl.opengl;

    exports sh.adelessfox.odradek.opengl;
    exports sh.adelessfox.odradek.opengl.rhi;
}
