import sh.adelessfox.odradek.game.ds2.rtti.callbacks.*;
import sh.adelessfox.odradek.game.ds2.rtti.data.MotionMatchingVecN;
import sh.adelessfox.odradek.game.ds2.rtti.data.ref.*;
import sh.adelessfox.odradek.game.ds2.rtti.extensions.EIndexFormatExtension;
import sh.adelessfox.odradek.game.ds2.rtti.extensions.ELanguageExtension;
import sh.adelessfox.odradek.game.ds2.rtti.extensions.GGUUIDExtension;
import sh.adelessfox.odradek.game.ds2.rtti.extensions.StreamingDataSourceExtension;
import sh.adelessfox.odradek.rtti.generator.TypeBindings;
import sh.adelessfox.odradek.rtti.generator.TypeBindings.Builtin;
import sh.adelessfox.odradek.rtti.generator.TypeBindings.Callback;
import sh.adelessfox.odradek.rtti.generator.TypeBindings.Extension;

import java.math.BigInteger;

@TypeBindings(
    input = @TypeBindings.Input(
        types = "types.json",
        extensions = "extensions.json"
    ),
    target = "sh.adelessfox.odradek.game.ds2.rtti.DS2",
    builtins = {
        // atoms
        @Builtin(type = "bool", repr = boolean.class),
        @Builtin(type = "int", repr = int.class),
        @Builtin(type = "int8", repr = byte.class),
        @Builtin(type = "int16", repr = short.class),
        @Builtin(type = "int32", repr = int.class),
        @Builtin(type = "int64", repr = long.class),
        @Builtin(type = "intptr", repr = long.class),
        @Builtin(type = "uint", repr = int.class),
        @Builtin(type = "uint8", repr = byte.class),
        @Builtin(type = "uint16", repr = short.class),
        @Builtin(type = "uint32", repr = int.class),
        @Builtin(type = "uint64", repr = long.class),
        @Builtin(type = "uint128", repr = BigInteger.class),
        @Builtin(type = "uintptr", repr = long.class),
        @Builtin(type = "float", repr = float.class),
        @Builtin(type = "double", repr = double.class),
        @Builtin(type = "HalfFloat", repr = float.class),
        @Builtin(type = "tchar", repr = char.class),
        @Builtin(type = "wchar", repr = char.class),
        @Builtin(type = "ucs4", repr = int.class),
        @Builtin(type = "String", repr = String.class),
        @Builtin(type = "WString", repr = String.class),
        @Builtin(type = "StringHash", repr = int.class),
        @Builtin(type = "MotionMatchingVecN", repr = MotionMatchingVecN.class),

        // pointers
        @Builtin(type = "Ref", repr = Ref.class),
        @Builtin(type = "StreamingRef", repr = StreamingRef.class),
        @Builtin(type = "UUIDRef", repr = UUIDRef.class),
        @Builtin(type = "WeakPtr", repr = WeakPtr.class),
        @Builtin(type = "cptr", repr = CPtr.class),
    },
    callbacks = {
        @Callback(type = "DataBufferResource", handler = DataBufferResourceCallback.class),
        @Callback(type = "IndexArrayResource", handler = IndexArrayResourceCallback.class),
        @Callback(type = "LocalizedTextResource", handler = LocalizedTextResourceCallback.class),
        // @Callback(type = "PhysicsShapeResource", handler = PhysicsShapeResourceCallback.class),
        @Callback(type = "ShaderResource", handler = ShaderResourceCallback.class),
        @Callback(type = "Texture", handler = TextureCallback.class),
        @Callback(type = "UITexture", handler = UITextureCallback.class),
        @Callback(type = "UITextureFrames", handler = UITextureFramesCallback.class),
        @Callback(type = "VertexArrayResource", handler = VertexArrayResourceCallback.class),
    },
    extensions = {
        @Extension(type = "EIndexFormat", extension = EIndexFormatExtension.class),
        @Extension(type = "ELanguage", extension = ELanguageExtension.class),
        @Extension(type = "GGUUID", extension = GGUUIDExtension.class),
        @Extension(type = "StreamingDataSource", extension = StreamingDataSourceExtension.class)
    }
)
module odradek.game.ds2 {
    requires static odradek.rtti.generator;

    requires odradek.core;
    requires odradek.middleware.edgeanim;
    requires odradek.middleware.jolt;
    requires odradek.middleware.riglogic;
    requires odradek.game;
    requires odradek.rtti;
    requires org.slf4j;

    opens sh.adelessfox.odradek.game.ds2.rtti to odradek.rtti;
    opens sh.adelessfox.odradek.game.ds2.rtti.callbacks to odradek.rtti;

    exports sh.adelessfox.odradek.game.ds2.game;
    exports sh.adelessfox.odradek.game.ds2.rtti.callbacks;
    exports sh.adelessfox.odradek.game.ds2.rtti.data.ref;
    exports sh.adelessfox.odradek.game.ds2.rtti.data;
    exports sh.adelessfox.odradek.game.ds2.rtti.extensions;
    exports sh.adelessfox.odradek.game.ds2.rtti;
    exports sh.adelessfox.odradek.game.ds2.storage;
    exports sh.adelessfox.odradek.game.ds2;

    provides sh.adelessfox.odradek.game.Converter with
        sh.adelessfox.odradek.game.ds2.converters.scene.MeshToSceneConverter,
        sh.adelessfox.odradek.game.ds2.converters.texture.TextureToTextureConverter,
        sh.adelessfox.odradek.game.ds2.converters.texture.UITextureToTextureConverter;

    provides sh.adelessfox.odradek.game.Game.Provider with
        sh.adelessfox.odradek.game.ds2.game.DS2Game.Provider;
}
