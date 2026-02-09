package sh.adelessfox.odradek.viewer.model.viewport2.layers;

import sh.adelessfox.odradek.viewer.model.viewport2.WgpuViewport;
import sh.adelessfox.wgpuj.objects.Device;
import sh.adelessfox.wgpuj.objects.Queue;
import sh.adelessfox.wgpuj.objects.RenderPass;

public interface Layer {
    void onAttach(WgpuViewport viewport, Device device, Queue queue);

    void onRender(WgpuViewport viewport, Queue queue, RenderPass pass, float delta);

    void onDetach();
}
