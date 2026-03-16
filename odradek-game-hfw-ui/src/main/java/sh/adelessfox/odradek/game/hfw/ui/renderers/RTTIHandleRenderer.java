package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.RTTIHandle;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.Renderer;

import java.util.Optional;

public final class RTTIHandleRenderer implements Renderer.OfObject<RTTIHandle, ForbiddenWestGame> {
    @Override
    public Optional<String> text(TypeInfo info, RTTIHandle object, ForbiddenWestGame game) {
        return Optional.of(object.staticTypeName());
    }
}
