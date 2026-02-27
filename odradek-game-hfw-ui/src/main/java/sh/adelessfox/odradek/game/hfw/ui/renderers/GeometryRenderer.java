package sh.adelessfox.odradek.game.hfw.ui.renderers;

import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.*;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.rtti.data.TypedObject;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;

import java.util.Optional;
import java.util.Set;

public final class GeometryRenderer implements Renderer.OfObject<TypedObject, ForbiddenWestGame> {
    private static final Set<Class<?>> TYPES = Set.of(
        IVec2.class, Vec2.class, Vec2Pack.class,
        IVec3.class, Vec3.class, Vec3Pack.class,
        IVec4.class, Vec4.class, Vec4Pack.class,
        ISize.class, FSize.class,
        IRect.class, FRect.class,
        IRange.class, FRange.class
    );

    @Override
    public Optional<StyledText> styledText(TypeInfo info, TypedObject object, ForbiddenWestGame game) {
        return switch (object) {
            case IVec2 vec -> vec(vec.x(), vec.y());
            case IVec3 vec -> vec(vec.x(), vec.y(), vec.z());
            case IVec4 vec -> vec(vec.x(), vec.y(), vec.z(), vec.w());
            case Vec2 vec -> vec(vec.x(), vec.y());
            case Vec3 vec -> vec(vec.x(), vec.y(), vec.z());
            case Vec4 vec -> vec(vec.x(), vec.y(), vec.z(), vec.w());
            case Vec2Pack vec -> vec(vec.x(), vec.y());
            case Vec3Pack vec -> vec(vec.x(), vec.y(), vec.z());
            case Vec4Pack vec -> vec(vec.x(), vec.y(), vec.z(), vec.w());
            case ISize size -> size(size.width(), size.height());
            case FSize size -> size(size.width(), size.height());
            case IRect rect -> rect(rect.left(), rect.top(), rect.right(), rect.bottom());
            case FRect rect -> rect(rect.left(), rect.top(), rect.right(), rect.bottom());
            case IRange range -> range(range.min(), range.max());
            case FRange range -> range(range.min(), range.max());
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
