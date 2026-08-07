package sh.adelessfox.odradek.export.bundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.game.ModelBundle;
import sh.adelessfox.odradek.scene.Scene;
import sh.adelessfox.odradek.texture.Texture;
import sh.adelessfox.odradek.texture.TextureSet;
import sh.adelessfox.odradek.util.Filenames;

import java.io.IOException;
import java.util.HashSet;

/**
 * Common behaviour for bundle exporters: emits one Cast file plus every source texture
 * from every TextureSet in the bundle. The texture format (DDS, PNG) is chosen by the
 * concrete subclass.
 */
abstract class AbstractModelBundleExporter implements Exporter.OfMultipleOutputs<ModelBundle> {
    private static final Logger log = LoggerFactory.getLogger(AbstractModelBundleExporter.class);
    private static final String CAST_EXPORTER_ID = "model.cast";
    private static final String MODEL_FILENAME = "model.cast";

    /** Id of the per-texture exporter to delegate to (e.g. "image.dds", "image.png"). */
    protected abstract String textureExporterId();

    /** File extension for emitted textures, including the leading dot. */
    protected abstract String textureExtension();

    @Override
    public Class<ModelBundle> supportedType() {
        return ModelBundle.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void export(ModelBundle bundle, OutputProvider provider) throws IOException {
        Exporter.OfSingleOutput<Scene> cast = (Exporter.OfSingleOutput<Scene>) Exporter.exporter(CAST_EXPORTER_ID)
            .filter(e -> e instanceof Exporter.OfSingleOutput<?>)
            .orElseThrow(() -> new IOException("Cast exporter (id=" + CAST_EXPORTER_ID + ") not available"));
        cast.export(bundle.scene(), provider.channel(MODEL_FILENAME));

        Exporter.OfSingleOutput<Texture> textureExporter = (Exporter.OfSingleOutput<Texture>) Exporter.exporter(textureExporterId())
            .filter(e -> e instanceof Exporter.OfSingleOutput<?>)
            .orElseThrow(() -> new IOException("Texture exporter (id=" + textureExporterId() + ") not available"));

        var writtenNames = new HashSet<String>();
        for (TextureSet set : bundle.textureSets()) {
            for (TextureSet.SourceTexture source : set.sourceTextures()) {
                var unpacked = set.unpack(source).orElse(null);
                if (unpacked == null) {
                    log.debug("Source texture {} ({}) not packed in any output texture; skipping", source.path(), source.type());
                    continue;
                }
                var name = Filenames.withSuffix(source.path(), textureExtension());
                if (!writtenNames.add(name)) {
                    continue;
                }
                textureExporter.export(unpacked, provider.channel(name));
            }
        }
    }
}
