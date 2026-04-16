package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.HFWGame;
import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.rtti.data.TypedObject;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;

import java.util.Optional;
import java.util.Set;

public final class GeometryRenderer implements Renderer.OfObject<TypedObject, HFWGame> {
    private static final Set<Class<?>> TYPES = Set.of(
        HFW.IVec2.class, HFW.Vec2.class, HFW.Vec2Pack.class,
        HFW.IVec3.class, HFW.Vec3.class, HFW.Vec3Pack.class,
        HFW.IVec4.class, HFW.Vec4.class, HFW.Vec4Pack.class,
        HFW.ISize.class, HFW.FSize.class,
        HFW.IRect.class, HFW.FRect.class,
        HFW.IRange.class, HFW.FRange.class
    );

    @Override
    public Optional<StyledText> styledText(TypeInfo info, TypedObject object, HFWGame game) {
        return switch (object) {
            case HFW.IVec2 vec -> vec(vec.x(), vec.y());
            case HFW.IVec3 vec -> vec(vec.x(), vec.y(), vec.z());
            case HFW.IVec4 vec -> vec(vec.x(), vec.y(), vec.z(), vec.w());
            case HFW.Vec2 vec -> vec(vec.x(), vec.y());
            case HFW.Vec3 vec -> vec(vec.x(), vec.y(), vec.z());
            case HFW.Vec4 vec -> vec(vec.x(), vec.y(), vec.z(), vec.w());
            case HFW.Vec2Pack vec -> vec(vec.x(), vec.y());
            case HFW.Vec3Pack vec -> vec(vec.x(), vec.y(), vec.z());
            case HFW.Vec4Pack vec -> vec(vec.x(), vec.y(), vec.z(), vec.w());
            case HFW.ISize size -> size(size.width(), size.height());
            case HFW.FSize size -> size(size.width(), size.height());
            case HFW.IRect rect -> rect(rect.left(), rect.top(), rect.right(), rect.bottom());
            case HFW.FRect rect -> rect(rect.left(), rect.top(), rect.right(), rect.bottom());
            case HFW.IRange range -> range(range.min(), range.max());
            case HFW.FRange range -> range(range.min(), range.max());
            default -> Optional.empty();
        };
    }

    @Override
    public boolean supports(TypeInfo info) {
        return TYPES.contains(info.type());
    }

    @SafeVarargs
    private static <T extends Number> Optional<StyledText> vec(T... values) {
        var builder = StyledText.builder()
            .add("(", StyledFragment.GRAYED);
        for (int i = 0; i < values.length; i++) {
            builder.add(String.valueOf(values[i]), StyledFragment.NUMBER);
            if (i < values.length - 1) {
                builder.add(", ", StyledFragment.GRAYED);
            }
        }
        return builder
            .add(")", StyledFragment.GRAYED)
            .build();
    }

    private static <T extends Number> Optional<StyledText> size(T width, T height) {
        return StyledText.builder()
            .add(String.valueOf(width), StyledFragment.NUMBER)
            .add(" x ", StyledFragment.GRAYED)
            .add(String.valueOf(height), StyledFragment.NUMBER)
            .build();
    }

    private static <T extends Number> Optional<StyledText> rect(T left, T top, T right, T bottom) {
        return StyledText.builder()
            .add(String.valueOf(left), StyledFragment.NUMBER)
            .add(", ", StyledFragment.GRAYED)
            .add(String.valueOf(top), StyledFragment.NUMBER)
            .add(" - ", StyledFragment.GRAYED)
            .add(String.valueOf(right), StyledFragment.NUMBER)
            .add(", ", StyledFragment.GRAYED)
            .add(String.valueOf(bottom), StyledFragment.NUMBER)
            .build();
    }

    private static <T extends Number> Optional<StyledText> range(T min, T max) {
        return StyledText.builder()
            .add(String.valueOf(min), StyledFragment.NUMBER)
            .add(" .. ", StyledFragment.GRAYED)
            .add(String.valueOf(max), StyledFragment.NUMBER)
            .build();
    }
}
