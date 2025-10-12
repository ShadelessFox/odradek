package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.data.riglogic.RigLogic;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.FacialAnimationDNAResource;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;
import java.nio.ByteOrder;

public class FacialAnimationDNAResourceCallback implements ExtraBinaryDataCallback<FacialAnimationDNAResource> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, FacialAnimationDNAResource object) throws IOException {
        reader.order(ByteOrder.BIG_ENDIAN);
        var rigLogic = RigLogic.read(reader); // FIXME: Not used now
        reader.order(ByteOrder.LITTLE_ENDIAN);
    }
}
