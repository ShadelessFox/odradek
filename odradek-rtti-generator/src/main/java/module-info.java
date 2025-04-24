module odradek.rtti.generator {
    requires static transitive java.compiler;
    requires static com.palantir.javapoet;
    requires static com.google.gson;

    requires odradek.core;
    requires odradek.rtti;

    exports sh.adelessfox.odradek.rtti.generator;
}
