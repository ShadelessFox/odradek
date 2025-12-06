module odradek.game.hfw.ui {
    requires odradek.game.hfw;
    requires odradek.ui;
    requires odradek.core;
    requires odradek.game;
    requires odradek.rtti;

    provides sh.adelessfox.odradek.ui.Renderer with
        sh.adelessfox.odradek.game.hfw.ui.renderers.GGUUIDRenderer,
        sh.adelessfox.odradek.game.hfw.ui.renderers.LocalizedTextResourceRenderer,
        sh.adelessfox.odradek.game.hfw.ui.renderers.MurmurHashValueRenderer,
        sh.adelessfox.odradek.game.hfw.ui.renderers.RenderTechniqueSet$AvailableTechniquesMaskRenderer,
        sh.adelessfox.odradek.game.hfw.ui.renderers.RenderTechniqueSet$InitiallyEnabledTechniquesMaskRenderer,
        sh.adelessfox.odradek.game.hfw.ui.renderers.ShaderTextureBindingRenderer,
        sh.adelessfox.odradek.game.hfw.ui.renderers.StreamingDataSourceRenderer,
        sh.adelessfox.odradek.game.hfw.ui.renderers.TextureSetEntry$PackingInfoRenderer,
        sh.adelessfox.odradek.game.hfw.ui.renderers.TextureSetTextureDescRenderer;
}
