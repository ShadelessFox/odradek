module odradek.rtti.generator {
    requires transitive java.compiler;
    requires com.google.gson;
    requires com.squareup.javapoet;
    requires org.slf4j;

    requires odradek.core;
    requires odradek.rtti;

    exports sh.adelessfox.odradek.rtti.generator;
}
