package sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision.shape;

public class DecoratedShape extends Shape {
    public Shape innerShape;

    @Override
    public void restoreSubShapeState(Shape[] subShapes) {
        assert subShapes.length == 1;
        innerShape = subShapes[0];
    }
}
