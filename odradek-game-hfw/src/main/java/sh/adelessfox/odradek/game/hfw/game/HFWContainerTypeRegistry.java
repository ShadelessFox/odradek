package sh.adelessfox.odradek.game.hfw.game;

import sh.adelessfox.odradek.game.decima.ContainerTypeRegistry;
import sh.adelessfox.odradek.game.decima.DecimaGame;
import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.rtti.data.TypedObject;

import java.util.Optional;
import java.util.Set;

public final class HFWContainerTypeRegistry implements ContainerTypeRegistry {
    // HFW lacks DS2's ArtPartsDataResource grouping; the top-level renderable
    // model containers are the skinned/static model resources directly. If a future
    // higher-level grouping type is discovered (e.g. for outfits/prefabs), add it here.
    private static final Set<String> CONTAINER_TYPES = Set.of(
        "SkinnedModelResource",
        "StaticModelResource"
    );

    @Override
    public boolean supports(DecimaGame game) {
        return game instanceof HFWGame;
    }

    @Override
    public Set<String> containerTypeNames() {
        return CONTAINER_TYPES;
    }

    @Override
    public Optional<String> nameOf(TypedObject object) {
        if (object instanceof HFW.TextureSet textureSet) {
            for (var desc : textureSet.textureDesc()) {
                var path = desc.path();
                if (path != null && !path.isEmpty()) {
                    return Optional.of(path.startsWith("work:") ? path.substring("work:".length()) : path);
                }
            }
        }
        return Optional.empty();
    }
}
