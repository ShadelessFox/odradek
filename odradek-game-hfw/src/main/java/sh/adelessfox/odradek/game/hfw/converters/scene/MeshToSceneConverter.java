package sh.adelessfox.odradek.game.hfw.converters.scene;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.HFWGame;
import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.game.hfw.rtti.data.ref.Ref;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.math.Matrix4f;
import sh.adelessfox.odradek.middleware.edgeanim.EdgeAnimJointTransform;
import sh.adelessfox.odradek.middleware.edgeanim.EdgeAnimSkeleton;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.scene.Joint;
import sh.adelessfox.odradek.scene.Node;
import sh.adelessfox.odradek.scene.Scene;
import sh.adelessfox.odradek.scene.Skin;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

public final class MeshToSceneConverter
    extends BaseSceneConverter<Object>
    implements Converter<Object, Scene, HFWGame> {

    private static final Logger log = LoggerFactory.getLogger(MeshToSceneConverter.class);

    @Override
    public Optional<Scene> convert(Object object, HFWGame game) {
        var context = new Context();
        var node = convertNodeIfAbsent(context, (HFW.RTTIRefObject) object, game);
        return node.map(Scene::of);
    }

    @Override
    public boolean supports(TypeInfo info) {
        Class<?> cls = info.type();
        return HFW.StaticMeshResource.class.isAssignableFrom(cls)
            || HFW.StaticMeshInstance.class.isAssignableFrom(cls)
            || HFW.RegularSkinnedMeshResource.class.isAssignableFrom(cls)
            || HFW.LodMeshResource.class.isAssignableFrom(cls)
            || HFW.MultiMeshResource.class.isAssignableFrom(cls)
            || HFW.BodyVariant.class.isAssignableFrom(cls)
            || HFW.SkinnedModelResource.class.isAssignableFrom(cls)
            || HFW.DestructibilityPart.class.isAssignableFrom(cls)
            || HFW.ControlledEntityResource.class.isAssignableFrom(cls)
            || HFW.PrefabResource.class.isAssignableFrom(cls)
            || HFW.PrefabInstance.class.isAssignableFrom(cls)
            || HFW.MockupGeometry.class.isAssignableFrom(cls)
            || HFW.HairResource.class.isAssignableFrom(cls);
    }

    private static Optional<Node> convertNodeIfAbsent(Context context, HFW.RTTIRefObject object, HFWGame game) {
        var key = object.general().objectUUID();
        var node = Optional.ofNullable(context.resources.get(key));
        if (node.isEmpty()) {
            node = convertNode(context, object, game);
        }
        node.ifPresent(n -> context.resources.put(key, n));
        return node;
    }

    private static Optional<Node> convertNode(Context context, HFW.RTTIRefObject object, HFWGame game) {
        return switch (object) {
            case HFW.StaticMeshResource r -> convertStaticMeshResource(context, r, game);
            case HFW.StaticMeshInstance r -> convertStaticMeshInstance(context, r, game);
            case HFW.RegularSkinnedMeshResource r -> convertRegularSkinnedMeshResource(context, r, game);
            case HFW.LodMeshResource r -> convertLodMeshResource(context, r, game);
            case HFW.MultiMeshResource r -> convertMultiMeshResource(context, r, game);
            case HFW.BodyVariant r -> convertBodyVariant(context, r, game);
            case HFW.SkinnedModelResource r -> convertSkinnedModelResource(context, r, game);
            case HFW.DestructibilityPart r -> convertDestructibilityPart(context, r, game);
            case HFW.ControlledEntityResource r -> convertControlledEntityResource(context, r, game);
            case HFW.PrefabResource r -> convertPrefabResource(context, r, game);
            case HFW.PrefabInstance r -> convertPrefabInstance(context, r, game);
            case HFW.MockupGeometry r -> convertMockupGeometry(context, r, game);
            case HFW.HairResource r -> convertHairResource(context, r, game);
            default -> {
                log.debug("Unsupported resource type: {}", object.getType());
                yield Optional.empty();
            }
        };
    }

    @SuppressWarnings("unused")
    private static Optional<Node> convertHairResource(Context context, HFW.HairResource resource, HFWGame game) {
        var pose = resource.geometry().poses().getFirst();
        var skin = convertHairPose(pose);

        var mesh = resource.geometry().meshLods().getFirst();
        var nodes = mesh.skinnedMeshes().stream()
            .map(m -> Node.builder()
                .mesh(convertHairMesh(m, game))
                .skin(skin)
                .build())
            .toList();

        return Node.of(nodes);
    }

    private static Skin convertHairPose(HFW.HairPose pose) {
        var joints = new ArrayList<Joint>();
        for (int i = 0; i < pose.bundles().size(); i++) {
            var bundle = pose.bundles().get(i);
            for (int j = 0; j < bundle.strands().size(); j++) {
                var strand = bundle.strands().get(j);
                for (int k = 0; k < strand.vertices().size(); k++) {
                    var vertex = strand.vertices().get(k);
                    if (k == 0) {
                        joints.add(new Joint(
                            OptionalInt.empty(),
                            "strand%d_%d".formatted(i, j),
                            Matrix4f.translation(vertex.x(), vertex.y(), vertex.z())
                        ));
                    } else {
                        var prev = strand.vertices().get(k - 1);
                        joints.add(new Joint(
                            OptionalInt.of(joints.size() - 1),
                            "strand%d_%d_%d".formatted(i, j, k),
                            Matrix4f.translation(vertex.x() - prev.x(), vertex.y() - prev.y(), vertex.z() - prev.z())
                        ));
                    }
                }
            }
        }
        return new Skin(joints);
    }

    private static Optional<Node> convertMockupGeometry(
        Context context,
        HFW.MockupGeometry geometry,
        HFWGame game
    ) {
        var node = convertNodeIfAbsent(context, geometry.staticMeshInstance().get(), game);
        var transform = geometry.general().orientation();
        return node.map(n -> n.transform(toMat4(transform)));
    }

    private static Optional<Node> convertPrefabResource(
        Context context,
        HFW.PrefabResource resource,
        HFWGame game
    ) {
        var collection = resource.general().objectCollection().get();
        var children = new ArrayList<Node>();

        for (var object : Ref.unwrap(collection.general().objects())) {
            convertNodeIfAbsent(context, object, game).ifPresent(children::add);
        }

        if (children.isEmpty()) {
            return Optional.empty();
        }

        return Node.of(children);
    }

    private static Optional<Node> convertPrefabInstance(
        Context context,
        HFW.PrefabInstance instance,
        HFWGame game
    ) {
        for (HFW.PrefabObjectOverrides override : instance.general().overrides()) {
            assert !override.isRemoved();
            assert !override.isTransformOverridden();
        }
        var node = convertNodeIfAbsent(context, instance.general().prefab().get(), game);
        var transform = instance.general().orientation();
        return node.map(n -> n.transform(toMat4(transform)));
    }

    private static Optional<Node> convertControlledEntityResource(
        Context context,
        HFW.ControlledEntityResource resource,
        HFWGame game
    ) {
        List<Node> children = new ArrayList<>();

        for (HFW.EntityComponentResource component : Ref.unwrap(resource.logic().entityComponentResources())) {
            switch (component) {
                case HFW.DestructibilityResource destructibility -> {
                    for (HFW.DestructibilityPart part : Ref.unwrap(destructibility.logic().convertedParts())) {
                        convertDestructibilityPart(context, part, game).ifPresent(children::add);
                        // TODO: Handle attachment joints
                    }
                }
                case HFW.SkinnedModelResource model -> {
                    convertSkinnedModelResource(context, model, game).ifPresent(children::add);
                }
                default -> log.debug("Skipping unsupported component: {}", component.getType());
            }
        }

        return Node.of(children);
    }

    private static Optional<Node> convertSkinnedModelResource(
        Context context,
        HFW.SkinnedModelResource resource,
        HFWGame game
    ) {
        var skin = convertSkeleton(resource.general().skeleton().get()).orElse(null);
        var parts = resource.general().modelPartResources().stream()
            .flatMap(part -> convertModelPartResource(context, part.get(), game).stream())
            .toList();

        var node = Node.builder()
            .skin(skin)
            .children(parts)
            .build();

        return Optional.of(node);
    }

    private static Optional<Node> convertDestructibilityPart(
        Context context,
        HFW.DestructibilityPart part,
        HFWGame game
    ) {
        var initialState = part.initialState().get();
        var modelPartResource = initialState.state().modelPartResource();
        if (modelPartResource != null) {
            return convertModelPartResource(context, modelPartResource.get(), game);
        }
        return Optional.empty();
    }

    private static Optional<Node> convertModelPartResource(
        Context context,
        HFW.ModelPartResource resource,
        HFWGame game
    ) {
        if (resource.general().meshResource() == null) {
            return Optional.empty();
        }
        return convertNodeIfAbsent(context, resource.general().meshResource().get(), game);
    }

    private static Optional<Node> convertStaticMeshInstance(
        Context context,
        HFW.StaticMeshInstance instance,
        HFWGame game
    ) {
        var node = convertNodeIfAbsent(context, instance.general().resource().get(), game);
        var transform = instance.general().orientation();
        return node.map(n -> n.transform(toMat4(transform)));
    }

    @SuppressWarnings("unused")
    private static Optional<Node> convertStaticMeshResource(
        Context context,
        HFW.StaticMeshResource resource,
        HFWGame game
    ) {
        if (resource.lighting().drawFlags().renderType() == HFW.EDrawPartType.ShadowCasterOnly) {
            log.debug("Skipping shadow caster mesh {}", resource.general().objectUUID().toDisplayString());
            return Optional.empty();
        }
        var mesh = convertMesh(
            resource.meshDescription().shadingGroups(),
            resource.meshDescription().primitives(),
            resource.meshDescription().streamingDataSource(),
            game
        );
        return Optional.of(Node.of(mesh));
    }

    @SuppressWarnings("unused")
    private static Optional<Node> convertRegularSkinnedMeshResource(
        Context context,
        HFW.RegularSkinnedMeshResource resource,
        HFWGame game
    ) {
        if (resource.lighting().drawFlags().renderType() == HFW.EDrawPartType.ShadowCasterOnly) {
            log.debug("Skipping shadow caster mesh {}", resource.general().objectUUID().toDisplayString());
            return Optional.empty();
        }
        var skin = convertSkeleton(resource.general().skeleton().get()).orElse(null);
        var mesh = convertMesh(
            resource.shadingGroups(),
            resource.primitives(),
            resource.streamingDataSource(),
            game
        );
        var node = Node.builder()
            .mesh(mesh)
            .skin(skin)
            .build();
        return Optional.of(node);
    }

    private static Optional<Skin> convertSkeleton(HFW.Skeleton skeleton) {
        if (!skeleton.general().hasBindPose()) {
            log.warn("Skipping skeleton {} without a bind pose", skeleton.general().objectUUID().toDisplayString());
            return Optional.empty();
        }

        List<EdgeAnimJointTransform> transforms;

        try (var reader = BinaryReader.wrap(skeleton.general().edgeAnimSkeleton())) {
            transforms = EdgeAnimSkeleton.read(reader).readBasePose(reader);
        } catch (IOException e) {
            log.error("Error reading edgeanim skeleton", e);
            return Optional.empty();
        }

        var joints = skeleton.general().joints();
        var converted = new ArrayList<Joint>();

        for (int i = 0; i < joints.size(); i++) {
            var joint = joints.get(i);
            converted.add(new Joint(
                joint.parentIndex() != -1 ? OptionalInt.of(joint.parentIndex()) : OptionalInt.empty(),
                joint.name(),
                transforms.get(i).toMatrix()));
        }

        return Optional.of(new Skin(converted));
    }

    private static Optional<Node> convertLodMeshResource(
        Context context,
        HFW.LodMeshResource resource,
        HFWGame game
    ) {
        var part = resource.runtimeMeshes().getFirst();
        return convertNodeIfAbsent(context, part.mesh().get(), game);
    }

    private static Optional<Node> convertMultiMeshResource(
        Context context,
        HFW.MultiMeshResource resource,
        HFWGame game
    ) {
        var meshes = resource.meshes();
        var transforms = resource.transforms();
        var children = IntStream.range(0, meshes.size())
            .mapToObj(i -> convertMultiMeshResourcePart(context,
                meshes.get(i).get(),
                transforms.isEmpty() ? null : transforms.get(i),
                game))
            .flatMap(Optional::stream)
            .toList();

        return Node.of(children);
    }

    private static Optional<Node> convertMultiMeshResourcePart(
        Context context,
        HFW.MeshResourceBase resource,
        HFW.Mat34 transform,
        HFWGame game
    ) {
        var child = convertNodeIfAbsent(context, resource, game);
        var matrix = transform != null ? toMat4(transform) : Matrix4f.identity();

        return child.map(c -> c.transform(matrix));
    }

    private static Optional<Node> convertBodyVariant(
        Context context,
        HFW.BodyVariant resource,
        HFWGame game
    ) {
        List<Node> children = resource.logic().modelPartResources().stream()
            .map(part -> convertNodeIfAbsent(context, part.get().general().meshResource().get(), game))
            .flatMap(Optional::stream)
            .toList();

        return Node.of(children);
    }

    private static final class Context {
        private final Map<HFW.GGUUID, Node> resources = new HashMap<>();
    }
}
