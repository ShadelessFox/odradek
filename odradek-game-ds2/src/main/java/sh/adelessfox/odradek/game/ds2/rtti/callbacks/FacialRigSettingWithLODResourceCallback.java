package sh.adelessfox.odradek.game.ds2.rtti.callbacks;

import sh.adelessfox.odradek.game.ds2.rtti.DS2.FacialRigSettingWithLODResource;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.middleware.riglogic.RigLogic;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public final class FacialRigSettingWithLODResourceCallback
    implements ExtraBinaryDataCallback<FacialRigSettingWithLODResource> {

    @Override
    public void deserialize(
        BinaryReader reader,
        TypeFactory factory,
        FacialRigSettingWithLODResource object
    ) throws IOException {
        // FIXME: Skipped for now
        RigLogic.read(reader);
    }
}
