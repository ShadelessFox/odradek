module odradek.export.shader {
    requires odradek.core;
    requires odradek.game;

    provides sh.adelessfox.odradek.game.Exporter with
        sh.adelessfox.odradek.export.shader.ShaderTextExporter,
        sh.adelessfox.odradek.export.shader.ShaderBlobExporter;
}
