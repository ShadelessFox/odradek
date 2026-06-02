package sh.adelessfox.odradek.animation;

import wtf.reversed.toolbox.math.Quaternion;
import wtf.reversed.toolbox.math.Vector3;

import java.util.List;

public sealed interface Track<T> {
    int boneId();

    List<KeyFrame<T>> keyFrames();

    record Translate(int boneId, List<KeyFrame<Vector3>> keyFrames) implements Track<Vector3> {
        public Translate {
            keyFrames = List.copyOf(keyFrames);
        }
    }

    record Rotate(int boneId, List<KeyFrame<Quaternion>> keyFrames) implements Track<Quaternion> {
        public Rotate {
            keyFrames = List.copyOf(keyFrames);
        }
    }

    record Scale(int boneId, List<KeyFrame<Vector3>> keyFrames) implements Track<Vector3> {
        public Scale {
            keyFrames = List.copyOf(keyFrames);
        }
    }
}
