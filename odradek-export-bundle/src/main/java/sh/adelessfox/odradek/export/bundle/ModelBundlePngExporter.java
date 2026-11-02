package sh.adelessfox.odradek.export.bundle;

import java.util.Optional;

public final class ModelBundlePngExporter extends AbstractModelBundleExporter {
    @Override
    public String id() {
        return "model.bundle.png";
    }

    @Override
    public String name() {
        return "Cast + PNG textures";
    }

    @Override
    public Optional<String> icon() {
        return Optional.of("fugue:paint-can");
    }

    @Override
    protected String textureExporterId() {
        return "image.png";
    }

    @Override
    protected String textureExtension() {
        return ".png";
    }
}
