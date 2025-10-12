module odradek.rtti.generator {
    requires transitive java.compiler;
    requires com.palantir.javapoet;
    requires com.google.gson;
    requires org.slf4j;

    requires odradek.core;
    requires odradek.rtti;

    exports sh.adelessfox.odradek.rtti.generator;
}
