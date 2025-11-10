import sh.adelessfox.odradek.game.hfw.converters.MeshToSceneConverter;
import sh.adelessfox.odradek.game.hfw.rtti.callbacks.*;
import sh.adelessfox.odradek.game.hfw.rtti.data.EIndexFormatExtension;
import sh.adelessfox.odradek.game.hfw.rtti.data.GGUUIDExtension;
import sh.adelessfox.odradek.game.hfw.rtti.data.MurmurHashValueExtension;
import sh.adelessfox.odradek.game.hfw.rtti.data.StreamingDataSourceExtension;
import sh.adelessfox.odradek.rtti.GenerateBindings;
import sh.adelessfox.odradek.rtti.GenerateBindings.Builtin;
import sh.adelessfox.odradek.rtti.GenerateBindings.Callback;
import sh.adelessfox.odradek.rtti.GenerateBindings.Extension;

import java.math.BigInteger;

@GenerateBindings(
    input = @GenerateBindings.Input(
        types = "types.json",
        extensions = "extensions.json"
    ),
    target = "sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest",
    builtins = {
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
    },
    callbacks = {
        @Callback(type = "DataBufferResource", handler = DataBufferResourceCallback.class),
        @Callback(type = "DebugMouseCursorPS4", handler = DebugMouseCursorPS4Callback.class),
        @Callback(type = "FacialAnimationDNAResource", handler = FacialAnimationDNAResourceCallback.class),
        @Callback(type = "IndexArrayResource", handler = IndexArrayResourceCallback.class),
        @Callback(type = "LocalizedTextResource", handler = LocalizedTextResourceCallback.class),
        @Callback(type = "MorphemeAnimation", handler = MorphemeAnimationCallback.class),
        @Callback(type = "MorphemeAsset", handler = MorphemeAssetCallback.class),
        @Callback(type = "MorphemeNetworkDefResource", handler = MorphemeNetworkDefResourceCallback.class),
        @Callback(type = "MorphemeNetworkInstancePreInitializedData", handler = MorphemeNetworkInstancePreInitializedDataCallback.class),
        @Callback(type = "PhysicsRagdollResource", handler = PhysicsRagdollResourceCallback.class),
        @Callback(type = "PhysicsShapeResource", handler = PhysicsShapeResourceCallback.class),
        @Callback(type = "Pose", handler = PoseCallback.class),
        @Callback(type = "ShaderResource", handler = ShaderResourceCallback.class),
        @Callback(type = "StaticTile", handler = StaticTileCallback.class),
        @Callback(type = "Texture", handler = TextureCallback.class),
        @Callback(type = "TextureList", handler = TextureListCallback.class),
        @Callback(type = "UITexture", handler = UITextureCallback.class),
        @Callback(type = "UITextureFrames", handler = UITextureFramesCallback.class),
        @Callback(type = "VertexArrayResource", handler = VertexArrayResourceCallback.class),
        @Callback(type = "WorldMapSuperTile", handler = WorldMapSuperTileCallback.class),
    },
    extensions = {
        @Extension(type = "EIndexFormat", extension = EIndexFormatExtension.class),
        @Extension(type = "GGUUID", extension = GGUUIDExtension.class),
        @Extension(type = "MurmurHashValue", extension = MurmurHashValueExtension.class),
        @Extension(type = "StreamingDataSource", extension = StreamingDataSourceExtension.class)
    }
)
module odradek.game.hfw {
    requires static odradek.rtti.generator;

    requires odradek.core;
    requires odradek.game;
    requires odradek.rtti;
    requires org.slf4j;

    opens sh.adelessfox.odradek.game.hfw.rtti to odradek.rtti;
    opens sh.adelessfox.odradek.game.hfw.rtti.callbacks to odradek.rtti;

    exports sh.adelessfox.odradek.game.hfw.game;
    exports sh.adelessfox.odradek.game.hfw.rtti.callbacks;
    exports sh.adelessfox.odradek.game.hfw.rtti.data;
    exports sh.adelessfox.odradek.game.hfw.rtti;
    exports sh.adelessfox.odradek.game.hfw.storage;
    exports sh.adelessfox.odradek.game.hfw;

    provides sh.adelessfox.odradek.game.Converter with
            MeshToSceneConverter,
        sh.adelessfox.odradek.game.hfw.converters.ObjectToTypedObjectConverter,
        sh.adelessfox.odradek.game.hfw.converters.StreamingDataSourceToBytesConverter,
        sh.adelessfox.odradek.game.hfw.converters.TextureToTextureConverter;
}
