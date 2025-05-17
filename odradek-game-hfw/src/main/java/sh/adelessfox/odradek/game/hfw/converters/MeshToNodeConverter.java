package sh.adelessfox.odradek.game.hfw.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.NotImplementedException;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.geometry.*;
import sh.adelessfox.odradek.math.Mat4;
import sh.adelessfox.odradek.rtti.data.Ref;
import sh.adelessfox.odradek.scene.Node;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.IntStream;

import static sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.*;

public final class MeshToNodeConverter implements Converter<ForbiddenWestGame, Node> {
    private static final Logger log = LoggerFactory.getLogger(MeshToNodeConverter.class);

    @Override
    public boolean supports(Object object) {
        return object instanceof StaticMeshResource
            || object instanceof RegularSkinnedMeshResource
            || object instanceof LodMeshResource
            || object instanceof MultiMeshResource
            || object instanceof BodyVariant
            || object instanceof SkinnedModelResource
            || object instanceof ControlledEntityResource;
    }

    @Override
    public Optional<Node> convert(Object object, ForbiddenWestGame game) {
        return switch (object) {
            case StaticMeshResource r -> convertStaticMeshResource(r, game);
            case RegularSkinnedMeshResource r -> convertRegularSkinnedMeshResource(r, game);
            case LodMeshResource r -> convertLodMeshResource(r, game);
            case MultiMeshResource r -> convertMultiMeshResource(r, game);
            case BodyVariant r -> convertBodyVariant(r, game);
            case SkinnedModelResource r -> convertSkinnedModelResource(r, game);
            case ControlledEntityResource r -> convertControlledEntityResource(r, game);
            default -> {
                log.error("Unsupported resource type: {}", object);
                yield Optional.empty();
            }
        };
    }

    private Optional<Node> convertControlledEntityResource(ControlledEntityResource resource, ForbiddenWestGame game) {
        List<Node> children = new ArrayList<>();

        for (EntityComponentResource component : Ref.unwrap(resource.logic().entityComponentResources())) {
            switch (component) {
                case DestructibilityResource destructibility -> {
                    for (DestructibilityPart part : Ref.unwrap(destructibility.logic().convertedParts())) {
                        var partState = part.initialState().get();
                        var modelPart = convertModelPartResource(partState.state().modelPartResource().get(), game);
                        // TODO: Handle attachment joints
                        modelPart.ifPresent(children::add);
                    }
                }
                case SkinnedModelResource model -> {
                    log.info("Skinned model: {}", model);
                    convertSkinnedModelResource(model, game).ifPresent(children::add);
                }
                default -> log.debug("Skipping unsupported component: {}", component.getType());
            }
        }

        return Optional.of(Node.of(children));
    }

    private Optional<Node> convertSkinnedModelResource(SkinnedModelResource resource, ForbiddenWestGame game) {
        var parts = resource.general().modelPartResources().stream()
            .flatMap(part -> convertModelPartResource(part.get(), game).stream())
            .toList();

        return Optional.of(Node.of(parts));
    }

    private Optional<Node> convertModelPartResource(ModelPartResource resource, ForbiddenWestGame game) {
        if (resource.general().meshResource() == null) {
            return Optional.empty();
        }
        return convert(resource.general().meshResource().get(), game);
    }

    private Optional<Node> convertStaticMeshResource(StaticMeshResource resource, ForbiddenWestGame game) {
        if (resource.meshDescription().isMoss()) {
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

    private Optional<Node> convertRegularSkinnedMeshResource(RegularSkinnedMeshResource resource, ForbiddenWestGame game) {
        var node = Node.builder()
            .mesh(convertMesh(resource.shadingGroups(), resource.primitives(), resource.streamingDataSource(), game))
            .skin(convertSkeleton(resource.general().skeleton().get(), resource.skinning().skinnedMeshJointBindings().get()))
            .build();

        return Optional.of(node);
    }

    private static Node convertSkeleton(Skeleton skeleton, SkinnedMeshIndexedJointBindings bindings) {
        var joints = skeleton.general().joints();
        var unlinked = joints.stream().map(joint -> Node.builder().name(joint.name())).toList();
        var linked = new Node[unlinked.size()];

        for (int i = 0; i < bindings.jointIndexList().length; i++) {
            var inverseBindMatrix = convertMat44(bindings.inverseBindMatrices().get(i));
            var node = unlinked.get(bindings.jointIndexList()[i]);
            node.matrix(inverseBindMatrix.invert());
        }

        for (int i = joints.size() - 1; i >= 0; i--) {
            var joint = joints.get(i);
            var node = unlinked.get(i).build();
            if (joint.parentIndex() != -1) {
                unlinked.get(joint.parentIndex()).add(node);
            }
            if (linked[i] != null) {
                throw new IllegalStateException("Node already linked");
            }
            linked[i] = node;
        }

        return Node.of(List.of(linked[0]));
    }

    private Optional<Node> convertLodMeshResource(LodMeshResource resource, ForbiddenWestGame game) {
        var part = resource.runtimeMeshes().getFirst();
        return convert(part.mesh().get(), game);
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
        var child = convert(resource, game);
        var matrix = transform != null ? convertMat34(transform) : Mat4.identity();

        return child.map(c -> c.transform(matrix));
    }

    private Optional<Node> convertBodyVariant(BodyVariant resource, ForbiddenWestGame game) {
        List<Node> children = resource.logic().modelPartResources().stream()
            .map(part -> convert(part.get().general().meshResource().get(), game))
            .flatMap(Optional::stream)
            .toList();

        return Optional.of(Node.of(children));
    }

    private static Mat4 convertMat34(Mat34 matrix) {
        return new Mat4(
            matrix.row0().x(), matrix.row1().x(), matrix.row2().x(), 0.f,
            matrix.row0().y(), matrix.row1().y(), matrix.row2().y(), 0.f,
            matrix.row0().z(), matrix.row1().z(), matrix.row2().z(), 0.f,
            matrix.row0().w(), matrix.row1().w(), matrix.row2().w(), 1.f
        );
    }

    private static Mat4 convertMat44(Mat44 matrix) {
        return new Mat4(
            matrix.col0().x(), matrix.col0().y(), matrix.col0().z(), matrix.col0().w(),
            matrix.col1().x(), matrix.col1().y(), matrix.col1().z(), matrix.col1().w(),
            matrix.col2().x(), matrix.col2().y(), matrix.col2().z(), matrix.col2().w(),
            matrix.col3().x(), matrix.col3().y(), matrix.col3().z(), matrix.col3().w()
        );
    }

    private Mesh convertMesh(
        List<Ref<ShadingGroup>> shadingGroups,
        List<Ref<PrimitiveResource>> primitiveResources,
        StreamingDataSource dataSource,
        ForbiddenWestGame game
    ) {
        var buffer = readDataSource(dataSource, game);
        var primitives = new ArrayList<Primitive>();

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

            primitives.add(new Primitive(indexAccessor, vertexAccessors));
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
            if (vertexArray.streaming()) {
                view = readDataAligned(buffer, count, stride);
            } else {
                view = ByteBuffer.wrap(stream.data());
            }

            for (var element : stream.elements()) {
                var offset = Byte.toUnsignedInt(element.offset());

                var semantic = switch (element.element()) {
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

                var accessor = switch (element.storageType()) {
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
        var component = switch (indexArray.format()) {
            case Index16 -> ComponentType.UNSIGNED_SHORT;
            case Index32 -> ComponentType.UNSIGNED_INT;
        };

        var count = indexArray.count();
        var stride = indexArray.format().stride();

        ByteBuffer view;
        if (indexArray.streaming()) {
            view = readDataAligned(buffer, count, stride);
        } else {
            view = ByteBuffer.wrap(indexArray.data());
        }

        return new Accessor(view, ElementType.SCALAR, component, startIndex * stride, endIndex - startIndex, stride);
    }

    private static ByteBuffer readDataAligned(ByteBuffer buffer, int count, int stride) {
        int position = alignUp(buffer.position(), stride);
        int size = count * stride;
        var view = buffer.slice(position, size);
        buffer.position(position + size);
        return view;
    }

    private static ByteBuffer readDataSource(StreamingDataSource dataSource, ForbiddenWestGame game) {
        if (dataSource.length() == 0) {
            return null;
        }
        try {
            return ByteBuffer.wrap(game.getStreamingSystem().getDataSourceData(dataSource));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static int alignUp(int value, int alignment) {
        return Math.ceilDiv(value, alignment) * alignment;
    }
}
