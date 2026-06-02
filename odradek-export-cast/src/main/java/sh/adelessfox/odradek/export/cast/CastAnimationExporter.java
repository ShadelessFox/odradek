package sh.adelessfox.odradek.export.cast;

import be.twofold.tinycast.CastNodes;
import sh.adelessfox.odradek.animation.Animation;
import sh.adelessfox.odradek.animation.Track;
import sh.adelessfox.odradek.game.Exporter;
import wtf.reversed.toolbox.math.Quaternion;
import wtf.reversed.toolbox.math.Vector3;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.function.Function;

public final class CastAnimationExporter
    extends BaseCastExporter<Animation>
    implements Exporter.OfSingleOutput<Animation> {

    @Override
    protected void export(Animation object, CastNodes.Root root) {
        var animation = root.createAnimation()
            .setFramerate(object.frameRate());

        for (Track<?> track : object.tracks()) {
            var bone = object.skeleton().bones().get(track.boneId());
            mapCurve(animation, track, bone.name());
        }
    }

    @Override
    public String id() {
        return "animation.cast";
    }

    private static void mapCurve(CastNodes.Animation animationNode, Track<?> track, String boneName) {
        switch (track) {
            case Track.Rotate rotate -> {
                createCurve(animationNode, boneName)
                    .setKeyPropertyName(CastNodes.KeyPropertyName.RQ)
                    .setKeyFrameBuffer(frames(rotate))
                    .setKeyValueBufferV4(rotation(rotate));
            }
            case Track.Translate translate -> {
                var frames = frames(translate);
                createCurve(animationNode, boneName)
                    .setKeyPropertyName(CastNodes.KeyPropertyName.TX)
                    .setKeyFrameBuffer(frames)
                    .setKeyValueBufferF32(translationScale(translate, Vector3::x));
                createCurve(animationNode, boneName)
                    .setKeyPropertyName(CastNodes.KeyPropertyName.TY)
                    .setKeyFrameBuffer(frames)
                    .setKeyValueBufferF32(translationScale(translate, Vector3::y));
                createCurve(animationNode, boneName)
                    .setKeyPropertyName(CastNodes.KeyPropertyName.TZ)
                    .setKeyFrameBuffer(frames)
                    .setKeyValueBufferF32(translationScale(translate, Vector3::z));
            }
            case Track.Scale scale -> {
                var frames = frames(scale);
                createCurve(animationNode, boneName)
                    .setKeyPropertyName(CastNodes.KeyPropertyName.SX)
                    .setKeyFrameBuffer(frames)
                    .setKeyValueBufferF32(translationScale(scale, Vector3::x));
                createCurve(animationNode, boneName)
                    .setKeyPropertyName(CastNodes.KeyPropertyName.SY)
                    .setKeyFrameBuffer(frames)
                    .setKeyValueBufferF32(translationScale(scale, Vector3::y));
                createCurve(animationNode, boneName)
                    .setKeyPropertyName(CastNodes.KeyPropertyName.SZ)
                    .setKeyFrameBuffer(frames)
                    .setKeyValueBufferF32(translationScale(scale, Vector3::z));
            }
        }
    }

    private static CastNodes.Curve createCurve(CastNodes.Animation animationNode, String boneName) {
        return animationNode.createCurve()
            .setNodeName(boneName)
            .setMode(CastNodes.Mode.ABSOLUTE);
    }

    private static FloatBuffer rotation(Track<Quaternion> rotation) {
        var result = FloatBuffer.allocate(rotation.keyFrames().size() * 4);
        rotation.keyFrames().forEach(frame -> frame.value().toBuffer(result));
        return result.flip();
    }

    private static FloatBuffer translationScale(Track<Vector3> track, Function<Vector3, Float> mapper) {
        var result = FloatBuffer.allocate(track.keyFrames().size());
        track.keyFrames().forEach(frame -> result.put(mapper.apply(frame.value())));
        return result.flip();
    }

    private static IntBuffer frames(Track<?> track) {
        var result = IntBuffer.allocate(track.keyFrames().size());
        track.keyFrames().forEach(frame -> result.put(frame.frame()));
        return result.flip();
    }
}
