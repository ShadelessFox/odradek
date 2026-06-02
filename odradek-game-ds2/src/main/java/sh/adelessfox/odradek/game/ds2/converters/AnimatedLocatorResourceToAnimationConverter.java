package sh.adelessfox.odradek.game.ds2.converters;

import sh.adelessfox.odradek.animation.Animation;
import sh.adelessfox.odradek.animation.KeyFrame;
import sh.adelessfox.odradek.animation.Track;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.scene.Bone;
import sh.adelessfox.odradek.scene.Skeleton;
import wtf.reversed.toolbox.math.*;

import java.util.*;

public final class AnimatedLocatorResourceToAnimationConverter
    implements Converter<DS2.AnimatedLocatorResource, Animation, DS2Game> {

    @Override
    public Optional<Animation> convert(DS2.AnimatedLocatorResource object, DS2Game game) {
        var tx = mapValue(object.logic().translateX());
        var ty = mapValue(object.logic().translateY());
        var tz = mapValue(object.logic().translateZ());
        var translate = merge(tx, ty, tz, Vector3::new);

        var rx = mapValue(object.logic().rotateX());
        var ry = mapValue(object.logic().rotateY());
        var rz = mapValue(object.logic().rotateZ());
        var rotate = merge(rx, ry, rz, (x, y, z) -> Quaternion.fromEuler(x, y, z, Angle.DEGREES, Order.XYZ));

        var root = new Bone(OptionalInt.empty(), "root", Matrix4.IDENTITY);
        var skeleton = new Skeleton(List.of(root));

        var animation = new Animation(
            skeleton,
            59.94006f,
            List.of(
                new Track.Translate(0, translate),
                new Track.Rotate(0, rotate)));

        return Optional.of(animation);
    }

    private List<KeyFrame<Float>> mapValue(DS2.AnimatableValue value) {
        var curve = value.animatedValue().get();
        if (curve == null) {
            return List.of(new KeyFrame<>(0, value.fixedValue()));
        }
        return curve.general().curvePoints().stream()
            .map(point -> new KeyFrame<>((int) point.position().x(), point.position().y()))
            .toList();
    }

    private static <T> List<KeyFrame<T>> merge(
        List<KeyFrame<Float>> xTrack,
        List<KeyFrame<Float>> yTrack,
        List<KeyFrame<Float>> zTrack,
        TernaryFunction<T> mapper
    ) {
        var frames = new TreeSet<Integer>();
        xTrack.forEach(k -> frames.add(k.frame()));
        yTrack.forEach(k -> frames.add(k.frame()));
        zTrack.forEach(k -> frames.add(k.frame()));

        var result = new ArrayList<KeyFrame<T>>();
        for (int frame : frames) {
            float x = evaluate(xTrack, frame);
            float y = evaluate(yTrack, frame);
            float z = evaluate(zTrack, frame);

            result.add(new KeyFrame<>(frame, mapper.apply(x, y, z)));
        }

        return result;
    }

    private static float evaluate(List<KeyFrame<Float>> frames, int frame) {
        if (frame <= frames.getFirst().frame()) {
            return frames.getFirst().value();
        }
        if (frame >= frames.getLast().frame()) {
            return frames.getLast().value();
        }

        int left = 0;
        int right = frames.size() - 1;

        while (left <= right) {
            int mid = (left + right) >>> 1;
            var key = frames.get(mid);

            if (key.frame() == frame) {
                return key.value();
            }

            if (key.frame() < frame) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        var a = frames.get(right);
        var b = frames.get(left);
        var t = (float) (frame - a.frame()) / (b.frame() - a.frame());

        return lerp(a.value(), b.value(), t);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    @FunctionalInterface
    private interface TernaryFunction<T> {
        T apply(float x, float y, float z);
    }
}
