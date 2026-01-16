module odradek.rtti {
    requires odradek.core;

    requires com.google.gson;
    requires java.compiler; // Required for javax.lang.model.SourceVersion
    requires org.slf4j;

    opens sh.adelessfox.odradek.rtti.data; // Required for equalsverifier

    exports sh.adelessfox.odradek.rtti.data;
    exports sh.adelessfox.odradek.rtti.factory;
    exports sh.adelessfox.odradek.rtti.io;
    exports sh.adelessfox.odradek.rtti.runtime;
    exports sh.adelessfox.odradek.rtti;
}
