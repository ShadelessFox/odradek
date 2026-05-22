package sh.adelessfox.odradek.viewer.model.viewport;

public final class ViewportContext {
    private boolean showBounds;
    private boolean showCameraOrigin;
    private boolean showSkins;
    private boolean showVertexColors;
    private boolean showVertexUVs;
    private boolean showWireframe;

    private float cameraMouseSensitivity = 1.0f;
    private float cameraSpeed = 5f;
    private float cameraShiftMultiplier = 5f;
    private float cameraCtrlMultiplier = 0.2f;

    public boolean isShowBounds() {
        return showBounds;
    }

    public void setShowBounds(boolean showBounds) {
        this.showBounds = showBounds;
    }

    public boolean isShowCameraOrigin() {
        return showCameraOrigin;
    }

    public void setShowCameraOrigin(boolean showCameraOrigin) {
        this.showCameraOrigin = showCameraOrigin;
    }

    public boolean isShowSkins() {
        return showSkins;
    }

    public void setShowSkins(boolean showSkins) {
        this.showSkins = showSkins;
    }

    public boolean isShowVertexColors() {
        return showVertexColors;
    }

    public void setShowVertexColors(boolean showVertexColors) {
        this.showVertexColors = showVertexColors;
    }

    public boolean isShowVertexUVs() {
        return showVertexUVs;
    }

    public void setShowVertexUVs(boolean showVertexUVs) {
        this.showVertexUVs = showVertexUVs;
    }

    public boolean isShowWireframe() {
        return showWireframe;
    }

    public void setShowWireframe(boolean showWireframe) {
        this.showWireframe = showWireframe;
    }

    public float getCameraMouseSensitivity() {
        return cameraMouseSensitivity;
    }

    public void setCameraMouseSensitivity(float cameraMouseSensitivity) {
        this.cameraMouseSensitivity = cameraMouseSensitivity;
    }

    public float getCameraSpeed() {
        return cameraSpeed;
    }

    public void setCameraSpeed(float cameraSpeed) {
        this.cameraSpeed = cameraSpeed;
    }

    public float getCameraShiftMultiplier() {
        return cameraShiftMultiplier;
    }

    public void setCameraShiftMultiplier(float cameraShiftMultiplier) {
        this.cameraShiftMultiplier = cameraShiftMultiplier;
    }

    public float getCameraCtrlMultiplier() {
        return cameraCtrlMultiplier;
    }

    public void setCameraCtrlMultiplier(float cameraCtrlMultiplier) {
        this.cameraCtrlMultiplier = cameraCtrlMultiplier;
    }
}
