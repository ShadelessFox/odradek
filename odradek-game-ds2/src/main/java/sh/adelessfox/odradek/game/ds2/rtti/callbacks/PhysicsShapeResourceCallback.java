package sh.adelessfox.odradek.game.ds2.rtti.callbacks;

import sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.collision.PhysicsMaterial;
import sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.collision.shape.Shape;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class PhysicsShapeResourceCallback implements ExtraBinaryDataCallback<DS2.PhysicsShapeResource> {
    @Override
    public void deserialize(BinarySource source, TypeFactory factory, DS2.PhysicsShapeResource object) throws IOException {
        var shapeMap = new ArrayList<Shape>();
        shapeMap.add(null);

        // FIXME: Materials must be derived from the object
        var materialMap = new ArrayList<PhysicsMaterial>();
        materialMap.add(null);

        // NOTE: Deserialized and thrown away; to serialize back, must be stored somewhere
        restoreFromBinaryState(source, shapeMap, materialMap);
    }

    private static Shape restoreFromBinaryState(
        BinarySource source,
        List<Shape> shapeMap,
        List<PhysicsMaterial> materialMap
    ) throws IOException {
        var shapeId = source.readInt();
        if (shapeId < shapeMap.size()) {
            return shapeMap.get(shapeId);
        }

        var shape = Shape.sRestoreFromBinaryState(source);
        for (int i = shapeMap.size(); i < shapeId; i++) {
            shapeMap.add(null);
        }

        assert shapeId == shapeMap.size();
        shapeMap.add(shape);

        var children = new Shape[source.readInt()];
        for (int i = 0; i < children.length; i++) {
            children[i] = restoreFromBinaryState(source, shapeMap, materialMap);
        }

        var materials = new PhysicsMaterial[source.readInt()];
        for (int i = 0; i < materials.length; i++) {
            int materialId = source.readInt();
            if (materialId == -1) {
                continue;
            }
            if (materialId < materialMap.size()) {
                materials[i] = materialMap.get(materialId);
            } else {
                // TODO: Materials are ignored for now
                // throw new NotImplementedException();
            }
        }

        shape.restoreSubShapeState(children);
        shape.restoreMaterialState(materials);

        return shape;
    }
}
