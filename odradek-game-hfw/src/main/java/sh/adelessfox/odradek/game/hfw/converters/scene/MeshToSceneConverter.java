package sh.adelessfox.odradek.game.hfw.converters.scene;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.data.edge.EdgeAnimJointTransform;
import sh.adelessfox.odradek.game.hfw.data.edge.EdgeAnimSkeleton;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.math.Matrix4f;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.rtti.data.Ref;
import sh.adelessfox.odradek.scene.Node;
import sh.adelessfox.odradek.scene.Scene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.*;

public final class MeshToSceneConverter
    extends BaseSceneConverter<Object>
    implements Converter<Object, Scene, ForbiddenWestGame> {

    private static final Logger log = LoggerFactory.getLogger(MeshToSceneConverter.class);

    @Override
    public Optional<Scene> convert(Object object, ForbiddenWestGame game) {
        return convertNode(object, game).map(Scene::of);
    }

    private static Optional<Node> convertNode(Object object, ForbiddenWestGame game) {
        return switch (object) {
            case StaticMeshResource r -> convertStaticMeshResource(r, game);
            case StaticMeshInstance r -> convertStaticMeshInstance(r, game);
            case RegularSkinnedMeshResource r -> convertRegularSkinnedMeshResource(r, game);
            case LodMeshResource r -> convertLodMeshResource(r, game);
            case MultiMeshResource r -> convertMultiMeshResource(r, game);
            case BodyVariant r -> convertBodyVariant(r, game);
            case SkinnedModelResource r -> convertSkinnedModelResource(r, game);
            case DestructibilityPart r -> convertDestructibilityPart(r, game);
            case ControlledEntityResource r -> convertControlledEntityResource(r, game);
            case PrefabResource r -> convertPrefabResource(r, game);
            case PrefabInstance r -> convertPrefabInstance(r, game);
            case MockupGeometry r -> convertMockupGeometry(r, game);
            default -> {
                log.debug("Unsupported resource type: {}", object);
                yield Optional.empty();
            }
        };
    }

    @Override
    public boolean supports(TypeInfo info) {
        Class<?> cls = info.type();
        return StaticMeshResource.class.isAssignableFrom(cls)
            || StaticMeshInstance.class.isAssignableFrom(cls)
            || RegularSkinnedMeshResource.class.isAssignableFrom(cls)
            || LodMeshResource.class.isAssignableFrom(cls)
            || MultiMeshResource.class.isAssignableFrom(cls)
            || BodyVariant.class.isAssignableFrom(cls)
            || SkinnedModelResource.class.isAssignableFrom(cls)
            || DestructibilityPart.class.isAssignableFrom(cls)
            || ControlledEntityResource.class.isAssignableFrom(cls)
            || PrefabResource.class.isAssignableFrom(cls)
            || PrefabInstance.class.isAssignableFrom(cls);
    }

    private static Optional<Node> convertMockupGeometry(MockupGeometry geometry, ForbiddenWestGame game) {
        var node = convertNode(geometry.staticMeshInstance().get(), game);
        var transform = geometry.general().orientation();
        return node.map(n -> n.transform(toMat4(transform)));
    }

    private static Optional<Node> convertPrefabResource(PrefabResource resource, ForbiddenWestGame game) {
        var collection = resource.general().objectCollection().get();
        var children = new ArrayList<Node>();

        for (var object : Ref.unwrap(collection.general().objects())) {
            convertNode(object, game).ifPresent(children::add);
        }

        if (children.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(Node.of(children));
    }

    private static Optional<Node> convertPrefabInstance(PrefabInstance instance, ForbiddenWestGame game) {
        for (PrefabObjectOverrides override : instance.general().overrides()) {
            assert !override.isRemoved();
            assert !override.isTransformOverridden();
        }
        var node = convertNode(instance.general().prefab().get(), game);
        var transform = instance.general().orientation();
        return node.map(n -> n.transform(toMat4(transform)));
    }

    private static Optional<Node> convertControlledEntityResource(ControlledEntityResource resource, ForbiddenWestGame game) {
        List<Node> children = new ArrayList<>();

        for (EntityComponentResource component : Ref.unwrap(resource.logic().entityComponentResources())) {
            switch (component) {
                case DestructibilityResource destructibility -> {
                    for (DestructibilityPart part : Ref.unwrap(destructibility.logic().convertedParts())) {
                        convertDestructibilityPart(part, game).ifPresent(children::add);
                        // TODO: Handle attachment joints
                    }
                }
                case SkinnedModelResource model -> {
                    convertSkinnedModelResource(model, game).ifPresent(children::add);
                }
                default -> log.debug("Skipping unsupported component: {}", component.getType());
            }
        }

        return Optional.of(Node.of(children));
    }

    private static Optional<Node> convertSkinnedModelResource(SkinnedModelResource resource, ForbiddenWestGame game) {
        var skin = convertSkeleton(resource.general().skeleton().get()).orElse(null);
        var parts = resource.general().modelPartResources().stream()
            .flatMap(part -> convertModelPartResource(part.get(), game).stream())
            .toList();

        var node = Node.builder()
            .skin(skin)
            .children(parts)
            .build();

        return Optional.of(node);
    }

    private static Optional<Node> convertDestructibilityPart(DestructibilityPart part, ForbiddenWestGame game) {
        var initialState = part.initialState().get();
        var modelPartResource = initialState.state().modelPartResource();
        if (modelPartResource != null) {
            return convertModelPartResource(modelPartResource.get(), game);
        }
        return Optional.empty();
    }

    private static Optional<Node> convertModelPartResource(ModelPartResource resource, ForbiddenWestGame game) {
        if (resource.general().meshResource() == null) {
            return Optional.empty();
        }
        return convertNode(resource.general().meshResource().get(), game);
    }

    private static Optional<Node> convertStaticMeshInstance(StaticMeshInstance instance, ForbiddenWestGame game) {
        var node = convertNode(instance.general().resource().get(), game);
        var transform = instance.general().orientation();
        return node.map(n -> n.transform(toMat4(transform)));
    }

    private static Optional<Node> convertStaticMeshResource(StaticMeshResource resource, ForbiddenWestGame game) {
        if (resource.lighting().drawFlags().renderType() == EDrawPartType.ShadowCasterOnly) {
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

    private static Optional<Node> convertRegularSkinnedMeshResource(RegularSkinnedMeshResource resource, ForbiddenWestGame game) {
        if (resource.lighting().drawFlags().renderType() == EDrawPartType.ShadowCasterOnly) {
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

    private static Optional<Node> convertSkeleton(Skeleton skeleton) {
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
        var unlinked = joints.stream().map(joint -> Node.builder().name(joint.name())).toList();

        for (int i = 0; i < joints.size(); i++) {
            unlinked.get(i).matrix(transforms.get(i).toMatrix());
        }

        var linked = new Node[joints.size()];

        for (int i = joints.size() - 1; i >= 0; i--) {
            var joint = joints.get(i);
            var node = unlinked.get(i).build();

            if (joint.parentIndex() != -1) {
                unlinked.get(joint.parentIndex()).add(node);
            }

            linked[i] = node;
        }

        return Optional.of(linked[0]);
    }

    private static Optional<Node> convertLodMeshResource(LodMeshResource resource, ForbiddenWestGame game) {
        var part = resource.runtimeMeshes().getFirst();
        return convertNode(part.mesh().get(), game);
    }

    private static Optional<Node> convertMultiMeshResource(MultiMeshResource resource, ForbiddenWestGame game) {
        var meshes = resource.meshes();
        var transforms = resource.transforms();
        var children = IntStream.range(0, meshes.size())
            .mapToObj(i -> convertMultiMeshResourcePart(meshes.get(i).get(), transforms.isEmpty() ? null : transforms.get(i), game))
            .flatMap(Optional::stream)
            .toList();

        return Optional.of(Node.of(children));
    }

    private static Optional<Node> convertMultiMeshResourcePart(MeshResourceBase resource, Mat34 transform, ForbiddenWestGame game) {
        var child = convertNode(resource, game);
        var matrix = transform != null ? toMat4(transform) : Matrix4f.identity();

        return child.map(c -> c.transform(matrix));
    }

    private static Optional<Node> convertBodyVariant(BodyVariant resource, ForbiddenWestGame game) {
        List<Node> children = resource.logic().modelPartResources().stream()
            .map(part -> convertNode(part.get().general().meshResource().get(), game))
            .flatMap(Optional::stream)
            .toList();

        return Optional.of(Node.of(children));
    }

}
