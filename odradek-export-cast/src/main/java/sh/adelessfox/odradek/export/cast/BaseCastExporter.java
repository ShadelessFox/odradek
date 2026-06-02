package sh.adelessfox.odradek.export.cast;

import be.twofold.tinycast.*;
import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.scene.Bone;
import sh.adelessfox.odradek.scene.Skeleton;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Optional;

abstract class BaseCastExporter<T> implements Exporter.OfSingleOutput<T> {
    @Override
    public void export(T object, WritableByteChannel channel) throws IOException {
        var cast = Cast.create();
        var root = cast.createRoot();

        root.createMetadata()
            .setSoftware("Odradek")
            .setAuthor("ShadelessFox");

        export(object, root);

        try {
            cast.write(Channels.newOutputStream(channel));
        } catch (CastException e) {
            throw new IOException("Error writing cast file", e);
        }
    }

    @Override
    public String name() {
        return "Cast (by DTZxPorter)";
    }

    @Override
    public String extension() {
        return "cast";
    }

    @Override
    public Optional<String> icon() {
        return Optional.of("fugue:paint-can");
    }

    protected abstract void export(T object, CastNodes.Root root);

    protected static void mapSkeleton(CastNodes.Skeleton skeletonNode, Skeleton skeleton) {
        for (Bone bone : skeleton.bones()) {
            mapBone(skeletonNode.createBone(), bone);
        }
    }

    protected static void mapBone(CastNodes.Bone boneNode, Bone bone) {
        boneNode.setName(bone.name());
        bone.parent().ifPresent(boneNode::setParentIndex);

        var transform = bone.matrix();
        var pos = transform.toTranslation();
        var rot = transform.toRotation();
        var scl = transform.toScale();

        boneNode.setLocalPosition(new Vec3(pos.x(), pos.y(), pos.z()));
        boneNode.setLocalRotation(new Vec4(rot.x(), rot.y(), rot.z(), rot.w()));
        boneNode.setScale(new Vec3(scl.x(), scl.y(), scl.z()));
    }
}
