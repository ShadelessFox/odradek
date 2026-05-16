package sh.adelessfox.odradek.texture.processing;

import sh.adelessfox.odradek.texture.Surface;
import sh.adelessfox.odradek.texture.TextureColorSpace;
import sh.adelessfox.odradek.texture.TextureFormat;
import sh.adelessfox.odradek.texture.processing.ops.Operation;
import wtf.reversed.toolbox.collect.Floats;

import java.util.List;
import java.util.stream.IntStream;

public final class Processor {
    private static final int TILE_SIZE = 256;

    public static Surface process(
        Surface source,
        TextureFormat format,
        TextureColorSpace colorSpace,
        List<? extends Operation> ops
    ) {
        var target = Surface.create(source.width(), source.height(), format, colorSpace);
        var unpacker = TileUnpacker.from(source);
        var packer = TilePacker.into(target);
        process(source.width(), source.height(), 1, unpacker, packer, ops);
        return target;
    }

    private static void process(
        int width,
        int height,
        int depth,
        TileUnpacker unpacker,
        TilePacker packer,
        List<? extends Operation> ops
    ) {
        var tilesX = Math.ceilDiv(width, TILE_SIZE);
        var tilesY = Math.ceilDiv(height, TILE_SIZE);

        IntStream.range(0, tilesX).boxed()
            .flatMap(x -> IntStream.range(0, tilesY).boxed()
                .flatMap(y -> IntStream.range(0, depth)
                    .mapToObj(z -> new int[]{x, y, z})))
            .parallel()
            .forEach(tile -> processTile(width, height, tile[0], tile[1], tile[2], unpacker, packer, ops));
    }

    private static void processTile(
        int width,
        int height,
        int x,
        int y,
        int z,
        TileUnpacker unpacker,
        TilePacker packer,
        List<? extends Operation> ops
    ) {
        int tw = Math.min(TILE_SIZE, width - x * TILE_SIZE);
        int th = Math.min(TILE_SIZE, height - y * TILE_SIZE);
        int sx = x * TILE_SIZE;
        int sy = y * TILE_SIZE;

        var buf = Floats.Mutable.allocate(TILE_SIZE * TILE_SIZE * 4);
        var ctx = new TileContext(sx, sy, z, tw, th);
        unpacker.unpack(ctx, buf);
        for (Operation op : ops) {
            op.process(ctx, buf);
        }
        packer.pack(ctx, buf);
    }
}
