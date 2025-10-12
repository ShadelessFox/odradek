module odradek.rtti {
    requires odradek.core;

    requires com.google.gson;
    requires java.compiler; // Required for javax.lang.model.SourceVersion
    requires org.slf4j;

    exports sh.adelessfox.odradek.rtti.data;
    exports sh.adelessfox.odradek.rtti.factory;
    exports sh.adelessfox.odradek.rtti.io;
    exports sh.adelessfox.odradek.rtti.runtime;
    exports sh.adelessfox.odradek.rtti;
}
