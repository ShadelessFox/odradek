package sh.adelessfox.odradek.animation;

import sh.adelessfox.odradek.scene.Skeleton;

import java.util.List;

public record Animation(
    Skeleton skeleton,
    float frameRate,
    List<Track<?>> tracks
) {
    public Animation {
        tracks = List.copyOf(tracks);
    }
}
