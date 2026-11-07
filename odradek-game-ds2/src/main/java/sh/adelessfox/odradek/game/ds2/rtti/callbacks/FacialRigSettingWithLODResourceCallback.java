package sh.adelessfox.odradek.game.ds2.rtti.callbacks;

import sh.adelessfox.odradek.game.ds2.middleware.riglogic.RigLogic;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public final class FacialRigSettingWithLODResourceCallback
    implements ExtraBinaryDataCallback<DS2.FacialRigSettingWithLODResource> {

    @Override
    public void deserialize(
        BinarySource source,
        TypeFactory factory,
        DS2.FacialRigSettingWithLODResource object
    ) throws IOException {
        // FIXME: Skipped for now
        RigLogic.read(source);
    }
}
