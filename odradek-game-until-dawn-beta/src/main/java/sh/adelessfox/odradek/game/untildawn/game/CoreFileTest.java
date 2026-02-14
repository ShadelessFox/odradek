package sh.adelessfox.odradek.game.untildawn.game;

import sh.adelessfox.odradek.game.untildawn.rtti.UntilDawnTypeFactory;
import sh.adelessfox.odradek.game.untildawn.rtti.UntilDawnTypeReader;
import sh.adelessfox.odradek.io.BinaryReader;

import java.nio.file.Path;
import java.util.List;

public class CoreFileTest {
    static void main() throws Exception {
        var typeFactory = new UntilDawnTypeFactory();
        var typeReader = new UntilDawnTypeReader();

        var lumps = Path.of("D:/PlayStation Games/Until Dawn TEST70001/PS3_GAME/USRDIR/data_ps3/LocalCachePS3/lumps");
        var path = lumps.resolve("characters.bodyvariants.samantha.samantha_naked_bv_assets_samanthanaked_concreteasset.core");

        try (BinaryReader reader = BinaryReader.open(path)) {
            List<Object> objects = typeReader.read(reader, typeFactory);
            System.out.println();
        }
    }
}
