module odradek.export.svg {
    requires odradek.core;
    requires odradek.game;
    requires java.desktop;

    provides sh.adelessfox.odradek.game.Exporter with
        sh.adelessfox.odradek.export.svg.SvgFontExporter;
}
