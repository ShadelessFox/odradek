package sh.adelessfox.odradek.texture;

import sh.adelessfox.odradek.texture.processing.Processor;
import sh.adelessfox.odradek.texture.processing.ops.Operation;

import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

final class TextureConverter {
    private TextureConverter() {
    }

    static Texture convert(Texture texture, TextureFormat target, Operation... ops) {
        if (texture.format() == target && ops.length == 0) {
            return texture;
        }
        var surfaces = texture.surfaces().stream()
            .map(surface -> convert(surface, target, ops))
            .toList();
        return new Texture(
            target,
            texture.kind(),
            texture.colorSpace(),
            surfaces,
            texture.mips(),
            texture.depth(),
            texture.arraySize(),
            texture.duration()
        );
    }

    static Surface convert(Surface surface, TextureFormat target, Operation... ops) {
        if (surface.format() == target && ops.length == 0) {
            return surface;
        }
        return Processor.process(surface, target, List.of(ops));
    }

    static Texture permute(
        Texture texture,
        Optional<Channel> red,
        Optional<Channel> green,
        Optional<Channel> blue,
        Optional<Channel> alpha
    ) {
        var channels = new EnumMap<Channel, Channel>(Channel.class);
        red.ifPresent(dst -> channels.put(Channel.R, dst));
        green.ifPresent(dst -> channels.put(Channel.G, dst));
        blue.ifPresent(dst -> channels.put(Channel.B, dst));
        alpha.ifPresent(dst -> channels.put(Channel.A, dst));

        var srcFormat = texture.format().isCompressed() ? texture.format().decompressed() : texture.format();
        var dstFormat = mapTargetFormat(channels.size(), srcFormat.bitsPerPixel() / srcFormat.channels());

        // NOTE: can detect and skip unnecessary permutations
        var permute = Operation.permute(builder -> {
            for (var entry : channels.entrySet()) {
                builder.channel(entry.getValue(), entry.getKey());
            }
        });

        return convert(texture, dstFormat, permute);
    }

    private static TextureFormat mapTargetFormat(int channels, int channelBits) {
        return switch (channels) {
            case 1 -> switch (channelBits) {
                case 8 -> TextureFormat.R8_UNORM;
                case 16 -> TextureFormat.R16_UNORM;
                case 32 -> TextureFormat.R32_SFLOAT;
                default -> throw new IllegalStateException("Unexpected channel bit depth: " + channelBits);
            };
            case 2 -> switch (channelBits) {
                case 8 -> TextureFormat.R8G8_UNORM;
                case 16 -> TextureFormat.R16G16_UNORM;
                case 32 -> TextureFormat.R32G32_SFLOAT;
                default -> throw new IllegalStateException("Unexpected channel bit depth: " + channelBits);
            };
            case 3 -> switch (channelBits) {
                case 8 -> TextureFormat.R8G8B8_UNORM;
                case 16 -> TextureFormat.R16G16B16_UNORM;
                case 32 -> TextureFormat.R32G32B32_SFLOAT;
                default -> throw new IllegalStateException("Unexpected channel bit depth: " + channelBits);
            };
            case 4 -> switch (channelBits) {
                case 8 -> TextureFormat.R8G8B8A8_UNORM;
                case 16 -> TextureFormat.R16G16B16A16_UNORM;
                case 32 -> TextureFormat.R32G32B32A32_SFLOAT;
                default -> throw new IllegalStateException("Unexpected channel bit depth: " + channelBits);
            };
            default -> throw new IllegalArgumentException("At least one channel must be mapped");
        };
    }
}
