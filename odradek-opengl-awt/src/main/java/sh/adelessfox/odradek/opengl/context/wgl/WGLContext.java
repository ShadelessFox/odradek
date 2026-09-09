package sh.adelessfox.odradek.opengl.context.wgl;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.windows.User32;
import org.lwjgl.system.windows.WNDCLASSEX;
import sh.adelessfox.odradek.opengl.context.GLContext;
import sh.adelessfox.odradek.opengl.context.GLProfile;

import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.WGL.wglDeleteContext;
import static org.lwjgl.opengl.WGL.wglMakeCurrent;
import static org.lwjgl.opengl.WGLARBCreateContext.*;
import static org.lwjgl.opengl.WGLARBCreateContextProfile.*;
import static org.lwjgl.opengl.WGLARBPbuffer.WGL_DRAW_TO_PBUFFER_ARB;
import static org.lwjgl.opengl.WGLARBPixelFormat.*;
import static org.lwjgl.system.windows.GDI32.SetPixelFormat;
import static org.lwjgl.system.windows.User32.*;
import static org.lwjgl.system.windows.WinBase.GetModuleHandle;

public final class WGLContext extends GLContext {
    private static final String WINDOW_CLASS = "odradek-gl";
    private static final int ERROR_CLASS_ALREADY_EXISTS = 0x582;

    private final long hwnd;
    private final long hdc;
    private final long hglrc;

    private WGLContext(long hwnd, long hdc, long hglrc) {
        this.hwnd = hwnd;
        this.hdc = hdc;
        this.hglrc = hglrc;
    }

    public static WGLContext create(GLProfile profile, int major, int minor, boolean debug) {
        var lastError = BufferUtils.createIntBuffer(1);

        var hinst = GetModuleHandle(lastError, (String) null);
        if (hinst == 0) {
            throw new IllegalStateException("GetModuleHandle failed: " + Integer.toHexString(lastError.get(0)));
        }

        try (var wc = WNDCLASSEX.calloc()) {
            wc.cbSize(wc.sizeof());
            wc.lpfnWndProc(User32::DefWindowProc);
            wc.hInstance(hinst);
            wc.lpszClassName(MemoryUtil.memUTF16(WINDOW_CLASS));

            if (RegisterClassEx(lastError, wc) == 0 && lastError.get(0) != ERROR_CLASS_ALREADY_EXISTS) {
                throw new IllegalStateException("RegisterClassEx failed: " + Integer.toHexString(lastError.get(0)));
            }
        }

        var hwnd = CreateWindowEx(lastError, 0, WINDOW_CLASS, "", WS_OVERLAPPEDWINDOW, 0, 0, 100, 100, 0, 0, hinst, 0);
        if (hwnd == 0) {
            throw new IllegalStateException("CreateWindowEx failed: " + Integer.toHexString(lastError.get(0)));
        }

        var hdc = GetDC(hwnd);
        var caps = new int[]{
            WGL_DRAW_TO_PBUFFER_ARB, GL_TRUE,
            WGL_SUPPORT_OPENGL_ARB, GL_TRUE,
            WGL_PIXEL_TYPE_ARB, WGL_TYPE_RGBA_ARB,
            WGL_COLOR_BITS_ARB, 32,
            WGL_DEPTH_BITS_ARB, 24,
            WGL_STENCIL_BITS_ARB, 8,
            0};

        var formats = new int[1];
        if (!wglChoosePixelFormatARB(hdc, caps, null, formats, new int[1])) {
            throw new IllegalStateException("wglChoosePixelFormatARB failed");
        }
        if (!SetPixelFormat(lastError, hdc, formats[0], null)) {
            throw new IllegalStateException("SetPixelFormat failed: " + Integer.toHexString(lastError.get(0)));
        }

        var attrs = new int[]{
            WGL_CONTEXT_MAJOR_VERSION_ARB, major,
            WGL_CONTEXT_MINOR_VERSION_ARB, minor,
            WGL_CONTEXT_PROFILE_MASK_ARB, profile == GLProfile.CORE ? WGL_CONTEXT_CORE_PROFILE_BIT_ARB : WGL_CONTEXT_COMPATIBILITY_PROFILE_BIT_ARB,
            WGL_CONTEXT_FLAGS_ARB, debug ? WGL_CONTEXT_DEBUG_BIT_ARB : 0,
            0};

        var hglrc = wglCreateContextAttribsARB(hdc, 0, attrs);
        if (hglrc == 0) {
            throw new IllegalStateException("wglCreateContextAttribsARB failed");
        }

        return new WGLContext(hwnd, hdc, hglrc);
    }

    @Override
    public void makeCurrent() {
        wglMakeCurrent(null, hdc, hglrc);
    }

    @Override
    public void clearCurrent() {
        wglMakeCurrent(null, 0, 0);
    }

    @Override
    public void dispose() {
        wglDeleteContext(null, hglrc);
        ReleaseDC(hwnd, hdc);
        DestroyWindow(null, hwnd);
    }
}
