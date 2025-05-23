module odradek.rtti.generator {
    requires transitive java.compiler;
    requires com.palantir.javapoet;
    requires com.google.gson;

    requires odradek.core;
    requires odradek.rtti;

    exports sh.adelessfox.odradek.rtti.generator;
}
