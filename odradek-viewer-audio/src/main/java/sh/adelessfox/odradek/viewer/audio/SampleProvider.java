package sh.adelessfox.odradek.viewer.audio;

interface SampleProvider {
    float getSample(int index, int channel);

    int getSampleCount();
}
