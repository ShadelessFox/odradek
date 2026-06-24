package sh.adelessfox.odradek.audio;

import wtf.reversed.toolbox.util.Check;

public record AudioFormat(int sampleRate, int channels) {
    public AudioFormat {
        Check.positive(sampleRate, "sampleRate");
        Check.positive(channels, "sampleRate");
    }
}
