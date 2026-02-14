package sh.adelessfox.odradek.game.untildawn.rtti.callbacks;

import sh.adelessfox.odradek.game.untildawn.rtti.UntilDawn.ProjMatrix;
import sh.adelessfox.odradek.game.untildawn.rtti.UntilDawn.SkinnedMeshBoneBindings;
import sh.adelessfox.odradek.game.untildawn.rtti.UntilDawnTypeReader;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class SkinnedMeshBoneBindingsCallback implements ExtraBinaryDataCallback<SkinnedMeshBoneBindings> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, SkinnedMeshBoneBindings object) throws IOException {
        var inverseBindMatrices = reader.readObjects(
            object.boneNames().size(),
            r -> UntilDawnTypeReader.readCompound(ProjMatrix.class, r, factory));
        // FIXME is the assertion even required?
        // object.inverseBindMatrices(inverseBindMatrices);
    }
}
