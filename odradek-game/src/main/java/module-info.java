import sh.adelessfox.odradek.game.Game;

module odradek.game {
    requires odradek.rtti;
    requires odradek.core;
    requires org.slf4j;

    exports sh.adelessfox.odradek.game;

    uses sh.adelessfox.odradek.game.Converter;
    uses sh.adelessfox.odradek.game.Exporter;
    uses Game.Provider;
}
