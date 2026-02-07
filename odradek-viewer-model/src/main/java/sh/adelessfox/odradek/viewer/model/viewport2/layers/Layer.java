package sh.adelessfox.odradek.viewer.model.viewport2.layers;

import sh.adelessfox.odradek.viewer.model.viewport2.Viewport2;
import sh.adelessfox.wgpuj.Device;
import sh.adelessfox.wgpuj.Queue;
import sh.adelessfox.wgpuj.RenderPass;

public interface Layer {
    void onAttach(Viewport2 viewport, Device device, Queue queue);

    void onRender(Viewport2 viewport, Queue queue, RenderPass pass, float delta);

    void onDetach();
}
