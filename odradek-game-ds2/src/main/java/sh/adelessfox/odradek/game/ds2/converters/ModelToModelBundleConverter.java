package sh.adelessfox.odradek.game.ds2.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.ModelBundle;
import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.game.decima.ObjectIdHolder;
import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.rtti.AtomTypeInfo;
import sh.adelessfox.odradek.rtti.ContainerTypeInfo;
import sh.adelessfox.odradek.rtti.PointerTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.rtti.data.TypedObject;
import sh.adelessfox.odradek.rtti.util.PathTypeVisitor;
import sh.adelessfox.odradek.rtti.util.TypePath;
import sh.adelessfox.odradek.scene.Scene;
import sh.adelessfox.odradek.texture.TextureSet;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Converts a DS2 model resource (ArtPartsDataResource, SkinnedModelResource, etc.) into a
 * {@link ModelBundle}: the Scene produced by the normal mesh pipeline, plus every TextureSet
 * reachable by walking outgoing references from the source object.
 */
public final class ModelToModelBundleConverter implements Converter<Object, ModelBundle, DS2Game> {
    private static final Logger log = LoggerFactory.getLogger(ModelToModelBundleConverter.class);
    private static final String TEXTURE_SET_TYPE = "TextureSet";

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

    @Override
    public Optional<ModelBundle> convert(Object object, DS2Game game) {
        if (!(object instanceof TypedObject typed)) {
            return Optional.empty();
        }

        var scene = Converter.convert(typed, Scene.class, game).orElse(null);
        if (scene == null) {
            return Optional.empty();
        }

        // Defer the BFS walk until the exporter actually asks for the textures.
        return Optional.of(new ModelBundle(scene, () -> resolveTextureSets(typed, game)));
    }

    private static List<TextureSet> resolveTextureSets(TypedObject root, DS2Game game) {
        var textureSetIds = collectReachableTextureSets(root, game);
        var textureSets = new ArrayList<TextureSet>(textureSetIds.size());
        for (ObjectId id : textureSetIds) {
            try {
                var tsObject = game.readObject(id);
                Converter.convert(tsObject, TextureSet.class, game)
                    .ifPresent(textureSets::add);
            } catch (IOException e) {
                log.warn("Failed to read TextureSet {}: {}", id, e.toString());
            }
        }
        return textureSets;
    }

    private static Set<ObjectId> collectReachableTextureSets(TypedObject root, DS2Game game) {
        var visited = new HashSet<ObjectId>();
        var textureSets = new LinkedHashSet<ObjectId>();
        var queue = new ArrayDeque<TypedObject>();
        queue.add(root);

        while (!queue.isEmpty()) {
            var current = queue.poll();
            var outgoing = new ArrayList<ObjectId>();
            collectOutgoingPointers(current, outgoing);

            for (ObjectId id : outgoing) {
                if (!visited.add(id)) {
                    continue;
                }
                var type = game.streamingGraph().group(id.groupId()).types().get(id.objectIndex());
                if (TEXTURE_SET_TYPE.equals(type.name())) {
                    textureSets.add(id);
                    continue;
                }
                try {
                    queue.add(game.readObject(id));
                } catch (IOException e) {
                    log.debug("Skipping unreadable object {} during TextureSet walk: {}", id, e.toString());
                }
            }
        }

        return textureSets;
    }

    private static void collectOutgoingPointers(TypedObject object, ArrayList<ObjectId> out) {
        var visitor = new PathTypeVisitor<RuntimeException>() {
            @Override
            public void visitContainer(ContainerTypeInfo typeInfo, Object object1, TypePath.Builder builder) {
                if (!(typeInfo.itemType() instanceof AtomTypeInfo)) {
                    super.visitContainer(typeInfo, object1, builder);
                }
            }

            @Override
            public void visitPointer(PointerTypeInfo typeInfo, Object object1, TypePath.Builder builder) {
                if (object1 instanceof ObjectIdHolder holder) {
                    out.add(holder.objectId());
                }
            }
        };
        visitor.visit(object);
    }
}
