package sh.adelessfox.odradek.game.ds2.ui.renderers;

import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.rtti.data.TypedObject;
import sh.adelessfox.odradek.ui.Renderer;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;

import java.util.Optional;
import java.util.Set;

public final class GeometryRenderer implements Renderer.OfObject<TypedObject, DS2Game> {
    private static final Set<Class<?>> TYPES = Set.of(
        DS2.IVec2.class, DS2.Vec2.class, DS2.Vec2Pack.class,
        DS2.IVec3.class, DS2.Vec3.class, DS2.Vec3Pack.class,
        DS2.IVec4.class, DS2.Vec4.class, DS2.Vec4Pack.class,
        DS2.ISize.class, DS2.FSize.class,
        DS2.IRect.class, DS2.FRect.class,
        DS2.IRange.class, DS2.FRange.class
    );

    @Override
    public Optional<StyledText> styledText(TypeInfo info, TypedObject object, DS2Game game) {
        return switch (object) {
            case DS2.IVec2 vec -> vec(vec.x(), vec.y());
            case DS2.IVec3 vec -> vec(vec.x(), vec.y(), vec.z());
            case DS2.IVec4 vec -> vec(vec.x(), vec.y(), vec.z(), vec.w());
            case DS2.Vec2 vec -> vec(vec.x(), vec.y());
            case DS2.Vec3 vec -> vec(vec.x(), vec.y(), vec.z());
            case DS2.Vec4 vec -> vec(vec.x(), vec.y(), vec.z(), vec.w());
            case DS2.Vec2Pack vec -> vec(vec.x(), vec.y());
            case DS2.Vec3Pack vec -> vec(vec.x(), vec.y(), vec.z());
            case DS2.Vec4Pack vec -> vec(vec.x(), vec.y(), vec.z(), vec.w());
            case DS2.ISize size -> size(size.width(), size.height());
            case DS2.FSize size -> size(size.width(), size.height());
            case DS2.IRect rect -> rect(rect.left(), rect.top(), rect.right(), rect.bottom());
            case DS2.FRect rect -> rect(rect.left(), rect.top(), rect.right(), rect.bottom());
            case DS2.IRange range -> range(range.min(), range.max());
            case DS2.FRange range -> range(range.min(), range.max());
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
