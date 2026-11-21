package sh.adelessfox.odradek.export.bundle;

import java.util.Optional;

public final class ModelBundleDdsExporter extends AbstractModelBundleExporter {
    @Override
    public String id() {
        return "model.bundle.dds";
    }

    @Override
    public String name() {
        return "Cast + DDS textures";
    }

    @Override
    public Optional<String> icon() {
        return Optional.of("fugue:paint-can");
    }

    @Override
    protected String textureExporterId() {
        return "image.dds";
    }

    @Override
    protected String textureExtension() {
        return ".dds";
    }
}
