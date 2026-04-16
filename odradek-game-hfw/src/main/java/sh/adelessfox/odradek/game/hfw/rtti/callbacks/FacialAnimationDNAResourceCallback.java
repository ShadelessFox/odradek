package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.middleware.riglogic.RigLogic;
import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class FacialAnimationDNAResourceCallback implements ExtraBinaryDataCallback<HFW.FacialAnimationDNAResource> {
    @Override
    public void deserialize(
        BinaryReader reader,
        TypeFactory factory,
        HFW.FacialAnimationDNAResource object
    ) throws IOException {
        // FIXME: Not used now
        RigLogic.read(reader);
    }
}
