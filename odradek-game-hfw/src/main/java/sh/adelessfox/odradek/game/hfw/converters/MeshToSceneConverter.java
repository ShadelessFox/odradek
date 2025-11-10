package sh.adelessfox.odradek.game.hfw.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.NotImplementedException;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.data.edge.EdgeAnimJointTransform;
import sh.adelessfox.odradek.game.hfw.data.edge.EdgeAnimSkeleton;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.geometry.*;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.math.Matrix4f;
import sh.adelessfox.odradek.rtti.data.Ref;
import sh.adelessfox.odradek.scene.Node;
import sh.adelessfox.odradek.scene.Scene;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.stream.IntStream;

import static sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.*;

public final class MeshToSceneConverter implements Converter<ForbiddenWestGame, Scene> {
    private static final Logger log = LoggerFactory.getLogger(MeshToSceneConverter.class);

    @Override
    public Optional<Scene> convert(Object object, ForbiddenWestGame game) {
        return convertNode(object, game).map(Scene::of);
    }

    private Optional<Node> convertNode(Object object, ForbiddenWestGame game) {
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
            default -> {
                log.error("Unsupported resource type: {}", object);
                yield Optional.empty();
            }
        };
    }

    @Override
    public Set<Class<?>> supportedTypes() {
        return Set.of(
            StaticMeshResource.class,
            StaticMeshInstance.class,
            RegularSkinnedMeshResource.class,
            LodMeshResource.class,
            MultiMeshResource.class,
            BodyVariant.class,
            SkinnedModelResource.class,
            DestructibilityPart.class,
            ControlledEntityResource.class,
            PrefabResource.class,
            PrefabInstance.class
        );
    }

    private Optional<Node> convertPrefabResource(PrefabResource resource, ForbiddenWestGame game) {
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

    private Optional<Node> convertPrefabInstance(PrefabInstance instance, ForbiddenWestGame game) {
        for (PrefabObjectOverrides override : instance.general().overrides()) {
            assert !override.isRemoved();
            assert !override.isTransformOverridden();
        }
        return convertNode(instance.general().prefab().get(), game);
    }

    private Optional<Node> convertControlledEntityResource(ControlledEntityResource resource, ForbiddenWestGame game) {
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

    private Optional<Node> convertSkinnedModelResource(SkinnedModelResource resource, ForbiddenWestGame game) {
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

    private Optional<Node> convertDestructibilityPart(DestructibilityPart part, ForbiddenWestGame game) {
        var initialState = part.initialState().get();
        var modePartResource = initialState.state().modelPartResource().get();
        return convertModelPartResource(modePartResource, game);
    }

    private Optional<Node> convertModelPartResource(ModelPartResource resource, ForbiddenWestGame game) {
        if (resource.general().meshResource() == null) {
            return Optional.empty();
        }
        return convertNode(resource.general().meshResource().get(), game);
    }

    private Optional<Node> convertStaticMeshInstance(StaticMeshInstance instance, ForbiddenWestGame game) {
        return convertNode(instance.general().resource().get(), game);
    }

    private Optional<Node> convertStaticMeshResource(StaticMeshResource resource, ForbiddenWestGame game) {
        var mesh = convertMesh(
            resource.meshDescription().shadingGroups(),
            resource.meshDescription().primitives(),
            resource.meshDescription().streamingDataSource(),
            game
        );
        return Optional.of(Node.of(mesh));
    }

    private Optional<Node> convertRegularSkinnedMeshResource(RegularSkinnedMeshResource resource, ForbiddenWestGame game) {
        var node = Node.builder()
            .mesh(convertMesh(resource.shadingGroups(), resource.primitives(), resource.streamingDataSource(), game))
            .build();

        return Optional.of(node);
    }

    private static Optional<Node> convertSkeleton(Skeleton skeleton) {
        if (!skeleton.general().hasBindPose()) {
            log.warn("Skeleton does not have a bind pose");
            return Optional.empty();
        }

        List<EdgeAnimJointTransform> transforms;

        try (var reader = BinaryReader.wrap(skeleton.general().edgeAnimSkeleton())) {
            transforms = EdgeAnimSkeleton.read(reader).readBasePose(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        var joints = skeleton.general().joints();
        var unlinked = joints.stream().map(joint -> Node.builder().name(joint.name())).toList();

        for (int i = 0; i < joints.size(); i++) {
            var joint = joints.get(i);
            var node = unlinked.get(i);
            var transform = transforms.get(i).toMatrix();

            if (joint.parentIndex() != -1) {
                var parent = unlinked.get(joint.parentIndex());
                node.matrix(parent.matrix().mul(transform));
            } else {
                node.matrix(transform);
            }
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

    private Optional<Node> convertLodMeshResource(LodMeshResource resource, ForbiddenWestGame game) {
        var part = resource.runtimeMeshes().getFirst();
        return convertNode(part.mesh().get(), game);
    }

    private Optional<Node> convertMultiMeshResource(MultiMeshResource resource, ForbiddenWestGame game) {
        var meshes = resource.meshes();
        var transforms = resource.transforms();
        var children = IntStream.range(0, meshes.size())
            .mapToObj(i -> convertMultiMeshResourcePart(meshes.get(i).get(), transforms.isEmpty() ? null : transforms.get(i), game))
            .flatMap(Optional::stream)
            .toList();

        return Optional.of(Node.of(children));
    }

    private Optional<Node> convertMultiMeshResourcePart(MeshResourceBase resource, Mat34 transform, ForbiddenWestGame game) {
        var child = convertNode(resource, game);
        var matrix = transform != null ? convertMat34(transform) : Matrix4f.identity();

        return child.map(c -> c.transform(matrix));
    }

    private Optional<Node> convertBodyVariant(BodyVariant resource, ForbiddenWestGame game) {
        List<Node> children = resource.logic().modelPartResources().stream()
            .map(part -> convertNode(part.get().general().meshResource().get(), game))
            .flatMap(Optional::stream)
            .toList();

        return Optional.of(Node.of(children));
    }

    private static Matrix4f convertMat34(Mat34 matrix) {
        return new Matrix4f(
            matrix.row0().x(), matrix.row1().x(), matrix.row2().x(), 0.f,
            matrix.row0().y(), matrix.row1().y(), matrix.row2().y(), 0.f,
            matrix.row0().z(), matrix.row1().z(), matrix.row2().z(), 0.f,
            matrix.row0().w(), matrix.row1().w(), matrix.row2().w(), 1.f
        );
    }

    private Mesh convertMesh(
        List<Ref<ShadingGroup>> shadingGroups,
        List<Ref<PrimitiveResource>> primitiveResources,
        StreamingDataSource dataSource,
        ForbiddenWestGame game
    ) {
        var buffer = readDataSource(dataSource, game);
        var primitives = new ArrayList<Primitive>(primitiveResources.size());

        assert shadingGroups.size() == primitiveResources.size();

        for (int i = 0; i < shadingGroups.size(); i++) {
            var primitive = primitiveResources.get(i).get();

            if (primitive.startIndex() > 0) {
                throw new NotImplementedException();
            }

            var vertexArray = primitive.vertexArray().get();
            var vertexAccessors = buildVertexAccessors(vertexArray, buffer);

            var indexArray = primitive.indexArray().get();
            var indexAccessor = buildIndexAccessor(indexArray, buffer, primitive.startIndex(), primitive.endIndex());

            primitives.add(new Primitive(indexAccessor, vertexAccessors, primitive.hashCode()));
        }

        if (buffer != null && buffer.hasRemaining()) {
            throw new IllegalStateException("Not all data was read from the buffer");
        }

        return Mesh.of(primitives);
    }

    private static Map<Semantic, Accessor> buildVertexAccessors(VertexArrayResource vertexArray, ByteBuffer buffer) {
        var accessors = new HashMap<Semantic, Accessor>();
        var count = vertexArray.count();

        for (var stream : vertexArray.streams()) {
            var stride = stream.stride();

            ByteBuffer view;
            if (vertexArray.isStreaming()) {
                view = readDataAligned(buffer, count, stride);
            } else {
                view = ByteBuffer
                    .wrap(stream.data())
                    .order(ByteOrder.LITTLE_ENDIAN);
            }

            for (var element : stream.elements()) {
                var offset = Byte.toUnsignedInt(element.offset());

                var semantic = switch (element.element().unwrap()) {
                    case Pos -> Semantic.POSITION;
                    case Tangent -> Semantic.TANGENT;
                    case Normal -> Semantic.NORMAL;
                    case Color -> Semantic.COLOR_0;
                    case UV0 -> Semantic.TEXTURE_0;
                    case UV1 -> Semantic.TEXTURE_1;
                    case BlendWeights -> Semantic.WEIGHTS_0;
                    case BlendWeights2 -> Semantic.WEIGHTS_1;
                    case BlendIndices -> Semantic.JOINTS_0;
                    case BlendIndices2 -> Semantic.JOINTS_1;
                    default -> {
                        log.warn("Skipping unsupported element (semantic: {})", element.element());
                        yield null;
                    }
                };

                if (semantic == null) {
                    continue;
                }

                var elementType = switch (element.slotsUsed()) {
                    case 1 -> ElementType.SCALAR;
                    case 2 -> ElementType.VEC2;
                    case 3 -> ElementType.VEC3;
                    case 4 -> ElementType.VEC4;
                    default -> {
                        log.warn("Skipping unsupported element (semantic: {}, size: {})", element.element(), element.slotsUsed());
                        yield null;
                    }
                };

                if (elementType == null) {
                    continue;
                }

                var accessor = switch (element.storageType().unwrap()) {
                    // @formatter:off
                    case UnsignedByte ->
                        new Accessor(view, elementType, ComponentType.UNSIGNED_BYTE, offset, count, stride, false);
                    case UnsignedByteNormalized ->
                        new Accessor(view, elementType, ComponentType.UNSIGNED_BYTE, offset, count, stride, true);
                    case UnsignedShort ->
                        new Accessor(view, elementType, ComponentType.UNSIGNED_SHORT, offset, count, stride, false);
                    case UnsignedShortNormalized ->
                        new Accessor(view, elementType, ComponentType.UNSIGNED_SHORT, offset, count, stride, true);
                    case SignedShort ->
                        new Accessor(view, elementType, ComponentType.SHORT, offset, count, stride, false);
                    case SignedShortNormalized ->
                        new Accessor(view, elementType, ComponentType.SHORT, offset, count, stride, true);
                    case HalfFloat ->
                        new Accessor(view, elementType, ComponentType.HALF_FLOAT, offset, count, stride, false);
                    case Float ->
                        new Accessor(view, elementType, ComponentType.FLOAT, offset, count, stride, false);
                    default -> {
                        log.warn("Skipping unsupported element (semantic: {}, format: {})", element.element(), element.storageType());
                        yield null;
                    }
                    // @formatter:on
                };

                if (accessor != null) {
                    accessors.put(semantic, accessor);
                }
            }
        }

        return accessors;
    }

    private static Accessor buildIndexAccessor(IndexArrayResource indexArray, ByteBuffer buffer, int startIndex, int endIndex) {
        var component = switch (indexArray.format().unwrap()) {
            case Index16 -> ComponentType.UNSIGNED_SHORT;
            case Index32 -> ComponentType.UNSIGNED_INT;
        };

        var count = indexArray.count();
        var stride = indexArray.format().unwrap().stride();

        ByteBuffer view;
        if (indexArray.isStreaming()) {
            view = readDataAligned(buffer, count, stride);
        } else {
            view = ByteBuffer
                .wrap(indexArray.data())
                .order(ByteOrder.LITTLE_ENDIAN);
        }

        return new Accessor(view, ElementType.SCALAR, component, startIndex * stride, endIndex - startIndex, stride);
    }

    private static ByteBuffer readDataAligned(ByteBuffer buffer, int count, int stride) {
        int position = alignUp(buffer.position(), stride);
        int size = count * stride;
        var view = buffer
            .slice(position, size)
            .order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(position + size);
        return view;
    }

    private static ByteBuffer readDataSource(StreamingDataSource dataSource, ForbiddenWestGame game) {
        if (dataSource.length() == 0) {
            return null;
        }
        try {
            return ByteBuffer
                .wrap(game.getStreamingSystem().getDataSourceData(dataSource))
                .order(ByteOrder.LITTLE_ENDIAN);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static int alignUp(int value, int alignment) {
        return Math.ceilDiv(value, alignment) * alignment;
    }
}
