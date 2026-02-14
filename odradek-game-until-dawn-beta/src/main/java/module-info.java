import sh.adelessfox.odradek.game.untildawn.rtti.callbacks.*;
import sh.adelessfox.odradek.game.untildawn.rtti.extensions.EIndexFormatExtension;
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
    target = "sh.adelessfox.odradek.game.untildawn.rtti.UntilDawn",
    builtins = {
        @Builtin(type = "wchar", repr = char.class),
        @Builtin(type = "int8", repr = byte.class),
        @Builtin(type = "uint8", repr = byte.class),
        @Builtin(type = "int16", repr = short.class),
        @Builtin(type = "uint16", repr = short.class),
        @Builtin(type = "int32", repr = int.class),
        @Builtin(type = "uint32", repr = int.class),
        @Builtin(type = "int", repr = int.class),
        @Builtin(type = "uint", repr = int.class),
        @Builtin(type = "int64", repr = long.class),
        @Builtin(type = "uint64", repr = long.class),
        @Builtin(type = "int128", repr = BigInteger.class),
        @Builtin(type = "uint128", repr = BigInteger.class),
        @Builtin(type = "HalfFloat", repr = float.class),
        @Builtin(type = "float", repr = float.class),
        @Builtin(type = "double", repr = double.class),
        @Builtin(type = "bool", repr = boolean.class),
        @Builtin(type = "String", repr = String.class),
        @Builtin(type = "WString", repr = String.class)
    },
    callbacks = {
        @Callback(type = "IndexArrayResource", handler = IndexArrayResourceCallback.class),
        @Callback(type = "PhysicsRagdollResource", handler = PhysicsRagdollResourceCallback.class),
        @Callback(type = "PhysicsSkeleton", handler = PhysicsSkeletonCallback.class),
        @Callback(type = "Pose", handler = PoseCallback.class),
        @Callback(type = "ShaderResource", handler = ShaderResourceCallback.class),
        @Callback(type = "SkinnedMeshBoneBindings", handler = SkinnedMeshBoneBindingsCallback.class),
        @Callback(type = "Texture", handler = TextureCallback.class),
        @Callback(type = "VertexArrayResource", handler = VertexArrayResourceCallback.class),
        @Callback(type = "WWiseSoundBankResource", handler = WWiseSoundBankResourceCallback.class)
    },
    extensions = {
        @Extension(type = "EIndexFormat", extension = EIndexFormatExtension.class),
    }
)
module odradek.game.untildawn_beta {
    requires static odradek.rtti.generator;

    requires odradek.core;
    requires odradek.game;
    requires odradek.rtti;
    requires org.slf4j;

    opens sh.adelessfox.odradek.game.untildawn.rtti to odradek.rtti;
    // opens sh.adelessfox.odradek.game.untildawn.rtti.callbacks to odradek.rtti;

    exports sh.adelessfox.odradek.game.untildawn.game;
    exports sh.adelessfox.odradek.game.untildawn.rtti;
}
