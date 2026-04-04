module odradek.game.ds2.ui {
    requires odradek.core;
    requires odradek.game.ds2;
    requires odradek.game;
    requires odradek.rtti;
    requires odradek.ui;

    provides sh.adelessfox.odradek.ui.Renderer with
        sh.adelessfox.odradek.game.ds2.ui.renderers.EnumFactEntryRenderer,
        sh.adelessfox.odradek.game.ds2.ui.renderers.GeometryRenderer,
        sh.adelessfox.odradek.game.ds2.ui.renderers.JointRenderer,
        sh.adelessfox.odradek.game.ds2.ui.renderers.LocalizedTextResourceRenderer,
        sh.adelessfox.odradek.game.ds2.ui.renderers.LocalizedTextResourceTextRenderer,
        sh.adelessfox.odradek.game.ds2.ui.renderers.GGUUIDRenderer,
        sh.adelessfox.odradek.game.ds2.ui.renderers.MurmurHashValueRenderer,
        sh.adelessfox.odradek.game.ds2.ui.renderers.RTTIHandleRenderer,
        sh.adelessfox.odradek.game.ds2.ui.renderers.ProgramParameterRenderer,
        sh.adelessfox.odradek.game.ds2.ui.renderers.ProgramResourceEntryPointRenderer,
        sh.adelessfox.odradek.game.ds2.ui.renderers.StreamingDataSourceRenderer,
        sh.adelessfox.odradek.game.ds2.ui.renderers.TextureSetTextureDescRenderer,
        sh.adelessfox.odradek.game.ds2.ui.renderers.attr.EnumFact$DefaultValueUUIDRenderer,
        sh.adelessfox.odradek.game.ds2.ui.renderers.attr.RenderTechniqueSet$AvailableTechniquesMaskRenderer,
        sh.adelessfox.odradek.game.ds2.ui.renderers.attr.RenderTechniqueSet$InitiallyEnabledTechniquesMaskRenderer;
}
