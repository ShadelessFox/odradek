package sh.adelessfox.odradek.app.ui.settings;

import sh.adelessfox.odradek.event.Event;

public sealed interface SettingsEvent extends Event {
    Settings settings();

    record AfterLoad(Settings settings) implements SettingsEvent {
    }

    record BeforeSave(Settings settings) implements SettingsEvent {
    }
}
