package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision.PhysicsMaterial;
import sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision.shape.Shape;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhysicsShapeResourceCallback implements ExtraBinaryDataCallback<PhysicsShapeResourceCallback.PhysicsShapeData> {
    public interface PhysicsShapeData {
    }

    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, PhysicsShapeData object) throws IOException {
        var shapeMap = new ArrayList<Shape>();
        shapeMap.add(null);

        // FIXME: Materials must be derived from the object
        var materialMap = new ArrayList<PhysicsMaterial>();
        materialMap.add(null);

        // FIXME: Skipped for now
        var shape = restoreFromBinaryState(reader, shapeMap, materialMap);
    }

    private static Shape restoreFromBinaryState(
        BinaryReader reader,
        List<Shape> shapeMap,
        List<PhysicsMaterial> materialMap
    ) throws IOException {
        var shapeId = reader.readInt();
        if (shapeId < shapeMap.size()) {
            return shapeMap.get(shapeId);
        }

        var shape = Shape.sRestoreFromBinaryState(reader);
        for (int i = shapeMap.size(); i < shapeId; i++) {
            shapeMap.add(null);
        }

        assert shapeId == shapeMap.size();
        shapeMap.add(shape);

        var children = new Shape[reader.readInt()];
        for (int i = 0; i < children.length; i++) {
            children[i] = restoreFromBinaryState(reader, shapeMap, materialMap);
        }

        var materials = new PhysicsMaterial[reader.readInt()];
        for (int i = 0; i < materials.length; i++) {
            int materialId = reader.readInt();
            if (materialId == ~0) {
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
