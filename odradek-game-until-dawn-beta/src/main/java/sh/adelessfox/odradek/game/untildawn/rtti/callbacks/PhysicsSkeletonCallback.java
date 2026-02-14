package sh.adelessfox.odradek.game.untildawn.rtti.callbacks;

import sh.adelessfox.odradek.game.untildawn.rtti.UntilDawn.PhysicsSkeleton;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class PhysicsSkeletonCallback implements ExtraBinaryDataCallback<PhysicsSkeleton> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, PhysicsSkeleton object) throws IOException {
        int count = reader.readInt();
        reader.align(16);
        object.data(reader.readBytes(count));
        reader.align(4);
    }
}
