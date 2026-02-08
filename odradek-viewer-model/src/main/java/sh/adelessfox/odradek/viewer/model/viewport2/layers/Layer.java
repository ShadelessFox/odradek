package sh.adelessfox.odradek.viewer.model.viewport2.layers;

import sh.adelessfox.odradek.viewer.model.viewport2.WgpuViewport;
import sh.adelessfox.wgpuj.Device;
import sh.adelessfox.wgpuj.Queue;
import sh.adelessfox.wgpuj.RenderPass;

public interface Layer {
    void onAttach(WgpuViewport viewport, Device device, Queue queue);

    void onRender(WgpuViewport viewport, Queue queue, RenderPass pass, float delta);

    void onDetach();
}
