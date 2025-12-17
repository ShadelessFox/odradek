package sh.adelessfox.odradek.viewer.texture;

import java.awt.image.RGBImageFilter;
import java.util.EnumSet;
import java.util.Set;

public class ChannelFilter extends RGBImageFilter {
    private final Set<Channel> channels;

    public ChannelFilter(Set<Channel> channels) {
        this.channels = EnumSet.copyOf(channels);
        this.canFilterIndexColorModel = true;
    }

    @Override
    public int filterRGB(int x, int y, int rgb) {
        int result = 0xff000000;

        switch (channels.size()) {
            case 0 -> { /* does nothing*/ }
            case 1 -> {
                var channel = channels.iterator().next();
                int color = channel.getComponent(rgb);
                result |= color << 16 | color << 8 | color;
            }
            default -> {
                for (Channel channel : channels) {
                    result = channel.setComponent(result, channel.getComponent(rgb));
                }
            }
        }

        return result;
    }
}
