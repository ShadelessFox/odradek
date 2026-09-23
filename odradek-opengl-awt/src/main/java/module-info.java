module odradek.opengl.awt {
    requires java.desktop;
    requires odradek.core;
    requires odradek.opengl;
    requires odradek.ui;
    requires org.lwjgl.jawt;
    requires org.lwjgl.opengl;
    requires org.lwjgl;
    requires org.slf4j;
    requires wtf.reversed.toolbox;

    exports sh.adelessfox.odradek.opengl.awt;
    exports sh.adelessfox.odradek.opengl.context;
}
