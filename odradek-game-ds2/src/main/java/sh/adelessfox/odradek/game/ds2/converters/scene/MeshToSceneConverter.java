package sh.adelessfox.odradek.game.ds2.converters.scene;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.game.ds2.rtti.data.ref.Ref;
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
    implements Converter<Object, Scene, DS2Game> {

    private static final Logger log = LoggerFactory.getLogger(MeshToSceneConverter.class);

    @Override
    public Optional<Scene> convert(Object object, DS2Game game) {
        var context = new Context();
        var node = convertNodeIfAbsent(context, (DS2.RTTIRefObject) object, game);
        return node.map(Scene::of);
    }

    @Override
    public boolean supports(TypeInfo info) {
        Class<?> cls = info.type();
        return DS2.StaticMeshResource.class.isAssignableFrom(cls)
            || DS2.StaticMeshInstance.class.isAssignableFrom(cls)
            || DS2.RegularSkinnedMeshResource.class.isAssignableFrom(cls)
            || DS2.LodMeshResource.class.isAssignableFrom(cls)
            || DS2.MultiMeshResource.class.isAssignableFrom(cls)
            || DS2.BodyVariant.class.isAssignableFrom(cls)
            || DS2.SkinnedModelResource.class.isAssignableFrom(cls)
            || DS2.DestructibilityPart.class.isAssignableFrom(cls)
            || DS2.ControlledEntityResource.class.isAssignableFrom(cls)
            || DS2.PrefabResource.class.isAssignableFrom(cls)
            || DS2.PrefabInstance.class.isAssignableFrom(cls)
            || DS2.MockupGeometry.class.isAssignableFrom(cls)
            || DS2.ArtPartsCoverModelSettingResource.class.isAssignableFrom(cls)
            || DS2.ArtPartsModelResource.class.isAssignableFrom(cls)
            || DS2.ArtPartsDataResource.class.isAssignableFrom(cls);
    }

    private static Optional<Node> convertNodeIfAbsent(Context context, DS2.RTTIRefObject object, DS2Game game) {
        var key = object.general().objectUUID();
        var node = Optional.ofNullable(context.resources.get(key));
        if (node.isEmpty()) {
            node = convertNode(context, object, game);
        }
        node.ifPresent(n -> context.resources.put(key, n));
        return node;
    }

    private static Optional<Node> convertNode(Context context, DS2.RTTIRefObject object, DS2Game game) {
        return switch (object) {
            case DS2.StaticMeshResource r -> convertStaticMeshResource(context, r, game);
            case DS2.StaticMeshInstance r -> convertStaticMeshInstance(context, r, game);
            case DS2.RegularSkinnedMeshResource r -> convertRegularSkinnedMeshResource(context, r, game);
            case DS2.LodMeshResource r -> convertLodMeshResource(context, r, game);
            case DS2.MultiMeshResource r -> convertMultiMeshResource(context, r, game);
            case DS2.BodyVariant r -> convertBodyVariant(context, r, game);
            case DS2.SkinnedModelResource r -> convertSkinnedModelResource(context, r, game);
            case DS2.DestructibilityPart r -> convertDestructibilityPart(context, r, game);
            case DS2.ControlledEntityResource r -> convertControlledEntityResource(context, r, game);
            case DS2.PrefabResource r -> convertPrefabResource(context, r, game);
            case DS2.PrefabInstance r -> convertPrefabInstance(context, r, game);
            case DS2.MockupGeometry r -> convertMockupGeometry(context, r, game);
            case DS2.ArtPartsCoverModelSettingResource r -> convertArtPartsCoverModelSettingResource(context, r, game);
            case DS2.ArtPartsModelResource r -> convertArtPartsModelResource(context, r, game);
            case DS2.ArtPartsDataResource r -> convertArtPartsDataResource(context, r, game);
            default -> {
                log.debug("Unsupported resource type: {}", object.getType());
                yield Optional.empty();
            }
        };
    }

    private static Optional<Node> convertArtPartsDataResource(
        Context context,
        DS2.ArtPartsDataResource resource,
        DS2Game game
    ) {
        var children = new ArrayList<Node>();

        for (var coverModel : Ref.unwrap(resource.coverModels().coverModelResources())) {
            convertNodeIfAbsent(context, coverModel, game).ifPresent(children::add);
        }

        if (resource.general().mainModelResource() != null) {
            var mainModel = resource.general().mainModelResource().get();
            convertNodeIfAbsent(context, mainModel, game).ifPresent(children::add);
        }
        if (resource.facialModels().facialAModelResource() != null) {
            var faceModel = resource.facialModels().facialAModelResource().get();
            convertNodeIfAbsent(context, faceModel, game).ifPresent(children::add);
        }
        if (resource.coverAndAnimModels().hairAModelResource() != null) {
            var hairModel = resource.coverAndAnimModels().hairAModelResource().get();
            convertNodeIfAbsent(context, hairModel, game).ifPresent(children::add);
        }
        if (resource.coverAndAnimModels().clothAModelResource() != null) {
            var clothAModel = resource.coverAndAnimModels().clothAModelResource().get();
            convertNodeIfAbsent(context, clothAModel, game).ifPresent(children::add);
        }
        if (resource.coverAndAnimModels().clothBModelResource() != null) {
            var clothBModel = resource.coverAndAnimModels().clothBModelResource().get();
            convertNodeIfAbsent(context, clothBModel, game).ifPresent(children::add);
        }
        if (resource.coverAndAnimModels().clothCModelResource() != null) {
            var clothCModel = resource.coverAndAnimModels().clothCModelResource().get();
            convertNodeIfAbsent(context, clothCModel, game).ifPresent(children::add);
        }

        if (children.isEmpty()) {
            return Optional.empty();
        }

        return Node.of(children);
    }

    private static Optional<Node> convertArtPartsCoverModelSettingResource(
        Context context,
        DS2.ArtPartsCoverModelSettingResource resource,
        DS2Game game
    ) {
        if (resource.general().modelResource() == null) {
            return Optional.empty();
        }
        return convertNodeIfAbsent(context, resource.general().modelResource().get(), game);
    }

    private static Optional<Node> convertArtPartsModelResource(
        Context context,
        DS2.ArtPartsModelResource resource,
        DS2Game game
    ) {
        var children = resource.general().expandedModelPartResources().stream()
            .map(part -> part.get().general().meshResource()).filter(Objects::nonNull)
            .map(mesh -> convertNodeIfAbsent(context, mesh.get(), game))
            .flatMap(Optional::stream)
            .toList();

        return Node.of(children);
    }

    private static Optional<Node> convertMockupGeometry(
        Context context,
        DS2.MockupGeometry geometry,
        DS2Game game
    ) {
        var node = convertNodeIfAbsent(context, geometry.staticMeshInstance().get(), game);
        var transform = geometry.general().orientation();
        return node.map(n -> n.transform(toMat4(transform)));
    }

    private static Optional<Node> convertPrefabResource(
        Context context,
        DS2.PrefabResource resource,
        DS2Game game
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
        DS2.PrefabInstance instance,
        DS2Game game
    ) {
        for (DS2.PrefabObjectOverrides override : instance.general().overrides()) {
            assert !override.isRemoved();
            assert !override.isTransformOverridden();
        }
        var node = convertNodeIfAbsent(context, instance.general().prefab().get(), game);
        var transform = instance.general().orientation();
        return node.map(n -> n.transform(toMat4(transform)));
    }

    private static Optional<Node> convertControlledEntityResource(
        Context context,
        DS2.ControlledEntityResource resource,
        DS2Game game
    ) {
        List<Node> children = new ArrayList<>();

        for (DS2.EntityComponentResource component : Ref.unwrap(resource.logic().entityComponentResources())) {
            switch (component) {
                case DS2.DestructibilityResource destructibility -> {
                    for (DS2.DestructibilityPart part : Ref.unwrap(destructibility.logic().convertedParts())) {
                        convertDestructibilityPart(context, part, game).ifPresent(children::add);
                        // TODO: Handle attachment joints
                    }
                }
                case DS2.SkinnedModelResource model -> {
                    convertSkinnedModelResource(context, model, game).ifPresent(children::add);
                }
                default -> log.debug("Skipping unsupported component: {}", component.getType());
            }
        }

        return Node.of(children);
    }

    private static Optional<Node> convertSkinnedModelResource(
        Context context,
        DS2.SkinnedModelResource resource,
        DS2Game game
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
        DS2.DestructibilityPart part,
        DS2Game game
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
        DS2.ModelPartResource resource,
        DS2Game game
    ) {
        if (resource.general().meshResource() == null) {
            return Optional.empty();
        }
        return convertNodeIfAbsent(context, resource.general().meshResource().get(), game);
    }

    private static Optional<Node> convertStaticMeshInstance(
        Context context,
        DS2.StaticMeshInstance instance,
        DS2Game game
    ) {
        var node = convertNodeIfAbsent(context, instance.general().resource().get(), game);
        var transform = instance.general().orientation();
        return node.map(n -> n.transform(toMat4(transform)));
    }

    @SuppressWarnings("unused")
    private static Optional<Node> convertStaticMeshResource(
        Context context,
        DS2.StaticMeshResource resource,
        DS2Game game
    ) {
        // TODO
        // if (resource.lighting().drawFlags().renderType() == EDrawPartType.ShadowCasterOnly) {
        //     log.debug("Skipping shadow caster mesh {}", resource.general().objectUUID().toDisplayString());
        //     return Optional.empty();
        // }
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
        DS2.RegularSkinnedMeshResource resource,
        DS2Game game
    ) {
        // TODO
        // if (resource.lighting().drawFlags().renderType() == EDrawPartType.ShadowCasterOnly) {
        //     log.debug("Skipping shadow caster mesh {}", resource.general().objectUUID().toDisplayString());
        //     return Optional.empty();
        // }
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

    private static Optional<Skin> convertSkeleton(DS2.Skeleton skeleton) {
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
        DS2.LodMeshResource resource,
        DS2Game game
    ) {
        var part = resource.runtimeMeshes().getFirst();
        return convertNodeIfAbsent(context, part.mesh().get(), game);
    }

    private static Optional<Node> convertMultiMeshResource(
        Context context,
        DS2.MultiMeshResource resource,
        DS2Game game
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
        DS2.MeshResourceBase resource,
        DS2.Mat34 transform,
        DS2Game game
    ) {
        var child = convertNodeIfAbsent(context, resource, game);
        var matrix = transform != null ? toMat4(transform) : Matrix4f.identity();

        return child.map(c -> c.transform(matrix));
    }

    private static Optional<Node> convertBodyVariant(
        Context context,
        DS2.BodyVariant resource,
        DS2Game game
    ) {
        List<Node> children = resource.logic().modelPartResources().stream()
            .map(part -> convertNodeIfAbsent(context, part.get().general().meshResource().get(), game))
            .flatMap(Optional::stream)
            .toList();

        return Node.of(children);
    }

    private static final class Context {
        private final Map<DS2.GGUUID, Node> resources = new HashMap<>();
    }
}
