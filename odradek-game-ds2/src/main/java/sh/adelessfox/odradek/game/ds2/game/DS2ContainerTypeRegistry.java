package sh.adelessfox.odradek.game.ds2.game;

import sh.adelessfox.odradek.game.decima.ContainerTypeRegistry;
import sh.adelessfox.odradek.game.decima.DecimaGame;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.rtti.data.TypedObject;

import java.util.Optional;
import java.util.Set;

public final class DS2ContainerTypeRegistry implements ContainerTypeRegistry {
    private static final Set<String> CONTAINER_TYPES = Set.of(
        "ArtPartsDataResource",
        "ArtPartsCoverModelSettingResource"
    );

    @Override
    public boolean supports(DecimaGame game) {
        return game instanceof DS2Game;
    }

    @Override
    public Set<String> containerTypeNames() {
        return CONTAINER_TYPES;
    }

    @Override
    public Optional<String> nameOf(TypedObject object) {
        if (object instanceof DS2.TextureSet textureSet) {
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
