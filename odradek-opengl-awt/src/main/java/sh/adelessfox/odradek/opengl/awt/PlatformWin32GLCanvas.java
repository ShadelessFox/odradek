package sh.adelessfox.odradek.opengl.awt;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Checks;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.jawt.JAWTDrawingSurface;
import org.lwjgl.system.jawt.JAWTDrawingSurfaceInfo;
import org.lwjgl.system.jawt.JAWTWin32DrawingSurfaceInfo;
import org.lwjgl.system.windows.PIXELFORMATDESCRIPTOR;
import org.lwjgl.system.windows.User32;
import org.lwjgl.system.windows.WNDCLASSEX;

import java.nio.IntBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.opengl.ARBMultisample.GL_SAMPLES_ARB;
import static org.lwjgl.opengl.ARBMultisample.GL_SAMPLE_BUFFERS_ARB;
import static org.lwjgl.opengl.ARBRobustness.GL_CONTEXT_FLAG_ROBUST_ACCESS_BIT_ARB;
import static org.lwjgl.opengl.GL11.GL_VERSION;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL43.GL_CONTEXT_FLAG_DEBUG_BIT;
import static org.lwjgl.opengl.WGL.*;
import static org.lwjgl.opengl.WGLARBContextFlushControl.*;
import static org.lwjgl.opengl.WGLARBCreateContext.*;
import static org.lwjgl.opengl.WGLARBCreateContextProfile.*;
import static org.lwjgl.opengl.WGLARBCreateContextRobustness.*;
import static org.lwjgl.opengl.WGLARBFramebufferSRGB.WGL_FRAMEBUFFER_SRGB_CAPABLE_ARB;
import static org.lwjgl.opengl.WGLARBMultisample.WGL_SAMPLES_ARB;
import static org.lwjgl.opengl.WGLARBMultisample.WGL_SAMPLE_BUFFERS_ARB;
import static org.lwjgl.opengl.WGLARBPixelFormat.*;
import static org.lwjgl.opengl.WGLARBPixelFormatFloat.WGL_TYPE_RGBA_FLOAT_ARB;
import static org.lwjgl.opengl.WGLARBRobustnessApplicationIsolation.WGL_CONTEXT_RESET_ISOLATION_BIT_ARB;
import static org.lwjgl.opengl.WGLEXTCreateContextES2Profile.WGL_CONTEXT_ES2_PROFILE_BIT_EXT;
import static org.lwjgl.opengl.WGLEXTFramebufferSRGB.WGL_FRAMEBUFFER_SRGB_CAPABLE_EXT;
import static org.lwjgl.system.APIUtil.APIVersion;
import static org.lwjgl.system.APIUtil.apiParseVersion;
import static org.lwjgl.system.JNI.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.system.windows.GDI32.*;
import static org.lwjgl.system.windows.User32.*;
import static org.lwjgl.system.windows.WindowsLibrary.HINSTANCE;
import static sh.adelessfox.odradek.opengl.awt.GLUtil.*;

/**
 * Windows-specific implementation of {@link PlatformGLCanvas}.
 *
 * @author Kai Burjack
 */
final class PlatformWin32GLCanvas extends PlatformGLCanvas {
    private JAWTDrawingSurface ds;
    private long hwnd;

    @Override
    protected long create(JAWTDrawingSurfaceInfo dsi, GLData data, GLData effective) {
        var dsiWin = JAWTWin32DrawingSurfaceInfo.create(dsi.platformInfo());
        this.hwnd = dsiWin.hwnd();
        try (MemoryStack stack = stackPush()) {
            long hwndDummy = createDummyWindow(stack);
            try {
                return create(stack, hwnd, hwndDummy, data, effective);
            } finally {
                DestroyWindow(null, hwndDummy);
            }
        }
    }

    /**
     * Encode the pixel format attributes stored in the given {@link GLData} into the given {@link IntBuffer} for wglChoosePixelFormatARB to consume.
     */
    private static void encodePixelFormatAttribs(IntBuffer ib, GLData attribs) {
        ib.put(WGL_DRAW_TO_WINDOW_ARB).put(1);
        ib.put(WGL_SUPPORT_OPENGL_ARB).put(1);
        ib.put(WGL_ACCELERATION_ARB).put(WGL_FULL_ACCELERATION_ARB);
        if (attribs.doubleBuffer) {
            ib.put(WGL_DOUBLE_BUFFER_ARB).put(1);
        }
        if (attribs.pixelFormatFloat) {
            ib.put(WGL_PIXEL_TYPE_ARB).put(WGL_TYPE_RGBA_FLOAT_ARB);
        } else {
            ib.put(WGL_PIXEL_TYPE_ARB).put(WGL_TYPE_RGBA_ARB);
        }
        if (attribs.redSize > 0) {
            ib.put(WGL_RED_BITS_ARB).put(attribs.redSize);
        }
        if (attribs.greenSize > 0) {
            ib.put(WGL_GREEN_BITS_ARB).put(attribs.greenSize);
        }
        if (attribs.blueSize > 0) {
            ib.put(WGL_BLUE_BITS_ARB).put(attribs.blueSize);
        }
        if (attribs.alphaSize > 0) {
            ib.put(WGL_ALPHA_BITS_ARB).put(attribs.alphaSize);
        }
        if (attribs.depthSize > 0) {
            ib.put(WGL_DEPTH_BITS_ARB).put(attribs.depthSize);
        }
        if (attribs.stencilSize > 0) {
            ib.put(WGL_STENCIL_BITS_ARB).put(attribs.stencilSize);
        }
        if (attribs.accumRedSize > 0) {
            ib.put(WGL_ACCUM_RED_BITS_ARB).put(attribs.accumRedSize);
        }
        if (attribs.accumGreenSize > 0) {
            ib.put(WGL_ACCUM_GREEN_BITS_ARB).put(attribs.accumGreenSize);
        }
        if (attribs.accumBlueSize > 0) {
            ib.put(WGL_ACCUM_BLUE_BITS_ARB).put(attribs.accumBlueSize);
        }
        if (attribs.accumAlphaSize > 0) {
            ib.put(WGL_ACCUM_ALPHA_BITS_ARB).put(attribs.accumAlphaSize);
        }
        if (attribs.accumRedSize > 0 || attribs.accumGreenSize > 0 || attribs.accumBlueSize > 0 || attribs.accumAlphaSize > 0) {
            ib.put(WGL_ACCUM_BITS_ARB).put(attribs.accumRedSize + attribs.accumGreenSize + attribs.accumBlueSize + attribs.accumAlphaSize);
        }
        if (attribs.sRGB) {
            ib.put(attribs.extBuffer_sRGB ? WGL_FRAMEBUFFER_SRGB_CAPABLE_EXT : WGL_FRAMEBUFFER_SRGB_CAPABLE_ARB).put(1);
        }
        if (attribs.samples > 0) {
            ib.put(WGL_SAMPLE_BUFFERS_ARB).put(1);
            ib.put(WGL_SAMPLES_ARB).put(attribs.samples);
        }
        ib.put(0);
    }

    private static long createDummyWindow(MemoryStack stack) {
        String className = "AWTAPPWNDCLASS";

        WNDCLASSEX in = WNDCLASSEX
            .calloc(stack)
            .cbSize(WNDCLASSEX.SIZEOF)
            .lpfnWndProc(User32::DefWindowProc)
            .hInstance(HINSTANCE)
            .lpszClassName(stack.UTF16(className));

        RegisterClassEx(null, in);
        return CreateWindowEx(null, WS_EX_APPWINDOW, className, "", 0, CW_USEDEFAULT, CW_USEDEFAULT,
            800, 600, NULL, NULL, HINSTANCE, NULL);
    }

    private static long create(MemoryStack stack, long windowHandle, long dummyWindowHandle, GLData attribs, GLData effective) {
        long bufferAddr = stack.nmalloc(4, (4 * 2) << 2);

        // Validate context attributes
        validateAttributes(attribs);

        // Find this exact pixel format, though for now without multisampling. This comes later!
        int flags = PFD_DRAW_TO_WINDOW | PFD_SUPPORT_OPENGL;
        if (attribs.doubleBuffer) {
            flags |= PFD_DOUBLEBUFFER;
        }
        if (attribs.stereo) {
            flags |= PFD_STEREO;
        }
        PIXELFORMATDESCRIPTOR pfd = PIXELFORMATDESCRIPTOR.calloc(stack)
            .nSize((short) PIXELFORMATDESCRIPTOR.SIZEOF)
            .nVersion((short) 1) // this should always be 1
            .dwLayerMask(PFD_MAIN_PLANE)
            .iPixelType(PFD_TYPE_RGBA)
            .dwFlags(flags)
            .cRedBits((byte) attribs.redSize)
            .cGreenBits((byte) attribs.greenSize)
            .cBlueBits((byte) attribs.blueSize)
            .cAlphaBits((byte) attribs.alphaSize)
            .cDepthBits((byte) attribs.depthSize)
            .cStencilBits((byte) attribs.stencilSize)
            .cAccumRedBits((byte) attribs.accumRedSize)
            .cAccumGreenBits((byte) attribs.accumGreenSize)
            .cAccumBlueBits((byte) attribs.accumBlueSize)
            .cAccumAlphaBits((byte) attribs.accumAlphaSize)
            .cAccumBits((byte) (attribs.accumRedSize + attribs.accumGreenSize + attribs.accumBlueSize + attribs.accumAlphaSize));
        long hDCdummy = GetDC(dummyWindowHandle);
        int pixelFormat = ChoosePixelFormat(null, hDCdummy, pfd);
        if (pixelFormat == 0 || !SetPixelFormat(null, hDCdummy, pixelFormat, pfd)) {
            // Pixel format unsupported
            ReleaseDC(dummyWindowHandle, hDCdummy);
            throw new IllegalStateException("Unsupported pixel format");
        }

        /*
         * Next, create a dummy context using Opengl32.lib's wglCreateContext. This should ALWAYS work, but won't give us a "new"/"core" context if we requested
         * that and also does not support multisampling. But we use this "dummy" context then to request the required WGL function pointers to create a new
         * OpenGL >= 3.0 context and with optional multisampling.
         */
        long dummyContext = wglCreateContext(null, hDCdummy);
        if (dummyContext == 0L) {
            ReleaseDC(dummyWindowHandle, hDCdummy);
            throw new IllegalStateException("Failed to create OpenGL context");
        }

        // Save current context to restore it later
        final long currentContext = wglGetCurrentContext(null);
        final long currentDc = wglGetCurrentDC();

        // Make the new dummy context current
        boolean success = wglMakeCurrent(null, hDCdummy, dummyContext);
        if (!success) {
            ReleaseDC(dummyWindowHandle, hDCdummy);
            wglDeleteContext(null, dummyContext);
            throw new IllegalStateException("Failed to make OpenGL context current");
        }

        // Query supported WGL extensions
        String wglExtensions = null;
        long wglGetExtensionsStringARBAddr = wglGetProcAddress(null, "wglGetExtensionsStringARB");
        if (wglGetExtensionsStringARBAddr != 0L) {
            long str = callPP(hDCdummy, wglGetExtensionsStringARBAddr);
            if (str != 0L) {
                wglExtensions = memASCII(str);
            } else {
                wglExtensions = "";
            }
        } else {
            // Try the EXT extension
            long wglGetExtensionsStringEXTAddr = wglGetProcAddress(null, "wglGetExtensionsStringEXT");
            if (wglGetExtensionsStringEXTAddr != 0L) {
                long str = callP(wglGetExtensionsStringEXTAddr);
                if (str != 0L) {
                    wglExtensions = memASCII(str);
                } else {
                    wglExtensions = "";
                }
            } else {
                wglExtensions = "";
            }
        }
        String[] splitted = wglExtensions.split(" ");
        Set<String> wglExtensionsList = new HashSet<>(splitted.length);
        Collections.addAll(wglExtensionsList, splitted);
        success = ReleaseDC(dummyWindowHandle, hDCdummy);
        if (!success) {
            wglDeleteContext(null, dummyContext);
            wglMakeCurrent(null, currentDc, currentContext);
            throw new IllegalStateException("Could not release dummy DC");
        }

        // For some constellations of context attributes, we can stop right here.
        if (!atLeast30(attribs.majorVersion, attribs.minorVersion) && attribs.samples == 0 && !attribs.sRGB && !attribs.pixelFormatFloat
            && attribs.contextReleaseBehavior == null && !attribs.robustness && attribs.api != GLData.API.GLES) {
            /* Finally, create the real context on the real window */
            long hDC = GetDC(windowHandle);
            SetPixelFormat(null, hDC, pixelFormat, pfd);
            success = wglDeleteContext(null, dummyContext);
            if (!success) {
                ReleaseDC(windowHandle, hDC);
                wglMakeCurrent(null, currentDc, currentContext);
                throw new IllegalStateException("Could not delete dummy GL context");
            }
            long context = wglCreateContext(null, hDC);

            if (attribs.swapInterval != null) {
                boolean has_WGL_EXT_swap_control = wglExtensionsList.contains("WGL_EXT_swap_control");
                if (!has_WGL_EXT_swap_control) {
                    ReleaseDC(windowHandle, hDC);
                    wglMakeCurrent(null, currentDc, currentContext);
                    wglDeleteContext(null, context);
                    throw new IllegalStateException("Swap interval requested but WGL_EXT_swap_control is unavailable");
                }
                if (attribs.swapInterval < 0) {
                    // Only allowed if WGL_EXT_swap_control_tear is available
                    boolean has_WGL_EXT_swap_control_tear = wglExtensionsList.contains("WGL_EXT_swap_control_tear");
                    if (!has_WGL_EXT_swap_control_tear) {
                        ReleaseDC(windowHandle, hDC);
                        wglMakeCurrent(null, currentDc, currentContext);
                        wglDeleteContext(null, context);
                        throw new IllegalStateException("Negative swap interval requested but WGL_EXT_swap_control_tear is unavailable");
                    }
                }
                // Make context current to set the swap interval
                success = wglMakeCurrent(null, hDC, context);
                if (!success) {
                    ReleaseDC(windowHandle, hDC);
                    wglMakeCurrent(null, currentDc, currentContext);
                    wglDeleteContext(null, context);
                    throw new IllegalStateException("Could not make GL context current");
                }
                long wglSwapIntervalEXTAddr = wglGetProcAddress(null, "wglSwapIntervalEXT");
                if (wglSwapIntervalEXTAddr != 0L) {
                    callI(attribs.swapInterval, wglSwapIntervalEXTAddr);
                }
            }

            /* Check if we want to share context */
            if (attribs.shareContext != null) {
                success = wglShareLists(null, attribs.shareContext.context, context);
                if (!success) {
                    ReleaseDC(windowHandle, hDC);
                    wglMakeCurrent(null, currentDc, currentContext);
                    wglDeleteContext(null, context);
                    throw new IllegalStateException("Failed while configuring context sharing");
                }
            }

            // Describe pixel format
            int pixFmtIndex = DescribePixelFormat(null, hDC, pixelFormat, pfd);
            if (pixFmtIndex == 0) {
                ReleaseDC(windowHandle, hDC);
                wglMakeCurrent(null, currentDc, currentContext);
                wglDeleteContext(null, context);
                throw new IllegalStateException("Failed to describe pixel format");
            }
            success = ReleaseDC(windowHandle, hDC);
            if (!success) {
                wglMakeCurrent(null, currentDc, currentContext);
                wglDeleteContext(null, context);
                throw new IllegalStateException("Could not release DC");
            }
            effective.redSize = pfd.cRedBits();
            effective.greenSize = pfd.cGreenBits();
            effective.blueSize = pfd.cBlueBits();
            effective.alphaSize = pfd.cAlphaBits();
            effective.depthSize = pfd.cDepthBits();
            effective.stencilSize = pfd.cStencilBits();
            int pixelFormatFlags = pfd.dwFlags();
            effective.doubleBuffer = (pixelFormatFlags & PFD_DOUBLEBUFFER) != 0;
            effective.stereo = (pixelFormatFlags & PFD_STEREO) != 0;
            effective.accumRedSize = pfd.cAccumRedBits();
            effective.accumGreenSize = pfd.cAccumGreenBits();
            effective.accumBlueSize = pfd.cAccumBlueBits();
            effective.accumAlphaSize = pfd.cAccumAlphaBits();

            // Restore old context
            wglMakeCurrent(null, currentDc, currentContext);
            return context;
        }

        // Check for WGL_ARB_create_context support
        if (!wglExtensionsList.contains("WGL_ARB_create_context")) {
            wglDeleteContext(null, dummyContext);
            wglMakeCurrent(null, currentDc, currentContext);
            throw new IllegalStateException("Extended context attributes requested but WGL_ARB_create_context is unavailable");
        }

        // Obtain wglCreateContextAttribsARB function pointer
        long wglCreateContextAttribsARBAddr = wglGetProcAddress(null, "wglCreateContextAttribsARB");
        if (wglCreateContextAttribsARBAddr == 0L) {
            wglDeleteContext(null, dummyContext);
            wglMakeCurrent(null, currentDc, currentContext);
            throw new IllegalStateException("WGL_ARB_create_context available but wglCreateContextAttribsARB is NULL");
        }

        IntBuffer attribList = BufferUtils.createIntBuffer(64);
        long attribListAddr = memAddress(attribList);
        long hDC = GetDC(windowHandle);

        // Obtain wglChoosePixelFormatARB if multisampling or sRGB or floating point pixel format is requested
        if (attribs.samples > 0 || attribs.sRGB || attribs.pixelFormatFloat) {
            long wglChoosePixelFormatAddr = wglGetProcAddress(null, "wglChoosePixelFormatARB");
            if (wglChoosePixelFormatAddr == 0L) {
                // Try EXT function (the WGL constants are the same in both extensions)
                wglChoosePixelFormatAddr = wglGetProcAddress(null, "wglChoosePixelFormatEXT");
                if (wglChoosePixelFormatAddr == 0L) {
                    ReleaseDC(windowHandle, hDC);
                    wglDeleteContext(null, dummyContext);
                    wglMakeCurrent(null, currentDc, currentContext);
                    throw new IllegalStateException("No support for wglChoosePixelFormatARB/EXT. Cannot query supported pixel formats.");
                }
            }
            if (attribs.samples > 0) {
                // Check for ARB or EXT extension (their WGL constants have the same value)
                boolean has_WGL_ARB_multisample = wglExtensionsList.contains("WGL_ARB_multisample");
                boolean has_WGL_EXT_multisample = wglExtensionsList.contains("WGL_EXT_multisample");
                if (!has_WGL_ARB_multisample && !has_WGL_EXT_multisample) {
                    ReleaseDC(windowHandle, hDC);
                    wglDeleteContext(null, dummyContext);
                    wglMakeCurrent(null, currentDc, currentContext);
                    throw new IllegalStateException("Multisampling requested but neither WGL_ARB_multisample nor WGL_EXT_multisample available");
                }
            }
            if (attribs.sRGB) {
                // Check for WGL_EXT_framebuffer_sRGB | WGL_ARB_framebuffer_sRGB
                boolean has_WGL_EXT_framebuffer_sRGB = wglExtensionsList.contains("WGL_EXT_framebuffer_sRGB"),
                    has_WGL_ARB_framebuffer_sRGB = wglExtensionsList.contains("WGL_ARB_framebuffer_sRGB");

                if (!(has_WGL_EXT_framebuffer_sRGB || has_WGL_ARB_framebuffer_sRGB)) {
                    ReleaseDC(windowHandle, hDC);
                    wglDeleteContext(null, dummyContext);
                    wglMakeCurrent(null, currentDc, currentContext);
                    throw new IllegalStateException("sRGB color space requested but WGL_EXT_framebuffer_sRGB is unavailable");
                }

                attribs.extBuffer_sRGB = has_WGL_EXT_framebuffer_sRGB;
            }
            if (attribs.pixelFormatFloat) {
                // Check for WGL_ARB_pixel_format_float
                boolean has_WGL_ARB_pixel_format_float = wglExtensionsList.contains("WGL_ARB_pixel_format_float");
                if (!has_WGL_ARB_pixel_format_float) {
                    ReleaseDC(windowHandle, hDC);
                    wglDeleteContext(null, dummyContext);
                    wglMakeCurrent(null, currentDc, currentContext);
                    throw new IllegalStateException("Floating-point format requested but WGL_ARB_pixel_format_float is unavailable");
                }
            }
            // Query matching pixel formats
            encodePixelFormatAttribs(attribList, attribs);
            success = callPPPPPI(hDC, attribListAddr, 0L, 1, bufferAddr + 4, bufferAddr, wglChoosePixelFormatAddr) == 1;
            int numFormats = memGetInt(bufferAddr);
            if (!success || numFormats == 0) {
                ReleaseDC(windowHandle, hDC);
                wglDeleteContext(null, dummyContext);
                wglMakeCurrent(null, currentDc, currentContext);
                throw new IllegalStateException("No supported pixel format found.");
            }
            pixelFormat = memGetInt(bufferAddr + 4);
            // Describe pixel format for the PIXELFORMATDESCRIPTOR to match the chosen format
            int pixFmtIndex = DescribePixelFormat(null, hDC, pixelFormat, pfd);
            if (pixFmtIndex == 0) {
                ReleaseDC(windowHandle, hDC);
                wglDeleteContext(null, dummyContext);
                wglMakeCurrent(null, currentDc, currentContext);
                throw new IllegalStateException("Failed to validate supported pixel format.");
            }
            // Obtain extended pixel format attributes
            long wglGetPixelFormatAttribivAddr = wglGetProcAddress(null, "wglGetPixelFormatAttribivARB");
            if (wglGetPixelFormatAttribivAddr == 0L) {
                // Try EXT function (function signature is the same)
                wglGetPixelFormatAttribivAddr = wglGetProcAddress(null, "wglGetPixelFormatAttribivEXT");
                if (wglGetPixelFormatAttribivAddr == 0L) {
                    ReleaseDC(windowHandle, hDC);
                    wglDeleteContext(null, dummyContext);
                    wglMakeCurrent(null, currentDc, currentContext);
                    throw new IllegalStateException("No support for wglGetPixelFormatAttribivARB/EXT. Cannot get effective pixel format attributes.");
                }
            }
            attribList.rewind();
            attribList
                .put(WGL_DOUBLE_BUFFER_ARB)
                .put(WGL_STEREO_ARB)
                .put(WGL_PIXEL_TYPE_ARB)
                .put(WGL_RED_BITS_ARB)
                .put(WGL_GREEN_BITS_ARB)
                .put(WGL_BLUE_BITS_ARB)
                .put(WGL_ALPHA_BITS_ARB)
                .put(WGL_ACCUM_RED_BITS_ARB)
                .put(WGL_ACCUM_GREEN_BITS_ARB)
                .put(WGL_ACCUM_BLUE_BITS_ARB)
                .put(WGL_ACCUM_ALPHA_BITS_ARB)
                .put(WGL_DEPTH_BITS_ARB)
                .put(WGL_STENCIL_BITS_ARB);
            IntBuffer attribValues = BufferUtils.createIntBuffer(attribList.position());
            long attribValuesAddr = memAddress(attribValues);
            success = callPPPI(hDC, pixelFormat, PFD_MAIN_PLANE, attribList.position(), attribListAddr,
                attribValuesAddr, wglGetPixelFormatAttribivAddr) == 1;
            if (!success) {
                ReleaseDC(windowHandle, hDC);
                wglDeleteContext(null, dummyContext);
                wglMakeCurrent(null, currentDc, currentContext);
                throw new IllegalStateException("Failed to get pixel format attributes.");
            }
            effective.doubleBuffer = attribValues.get(0) == 1;
            effective.stereo = attribValues.get(1) == 1;
            int pixelType = attribValues.get(2);
            effective.pixelFormatFloat = pixelType == WGL_TYPE_RGBA_FLOAT_ARB;
            effective.redSize = attribValues.get(3);
            effective.greenSize = attribValues.get(4);
            effective.blueSize = attribValues.get(5);
            effective.alphaSize = attribValues.get(6);
            effective.accumRedSize = attribValues.get(7);
            effective.accumGreenSize = attribValues.get(8);
            effective.accumBlueSize = attribValues.get(9);
            effective.accumAlphaSize = attribValues.get(10);
            effective.depthSize = attribValues.get(11);
            effective.stencilSize = attribValues.get(12);
        }

        // Compose the attributes list
        attribList.rewind();
        if (attribs.api == GLData.API.GL && atLeast30(attribs.majorVersion, attribs.minorVersion) || attribs.api == GLData.API.GLES && attribs.majorVersion > 0) {
            attribList.put(WGL_CONTEXT_MAJOR_VERSION_ARB).put(attribs.majorVersion);
            attribList.put(WGL_CONTEXT_MINOR_VERSION_ARB).put(attribs.minorVersion);
        }
        int profile = 0;
        if (attribs.api == GLData.API.GL) {
            if (attribs.profile == GLData.Profile.COMPATIBILITY) {
                profile = WGL_CONTEXT_COMPATIBILITY_PROFILE_BIT_ARB;
            } else if (attribs.profile == GLData.Profile.CORE) {
                profile = WGL_CONTEXT_CORE_PROFILE_BIT_ARB;
            }
        } else if (attribs.api == GLData.API.GLES) {
            boolean has_WGL_EXT_create_context_es2_profile = wglExtensionsList.contains("WGL_EXT_create_context_es2_profile");
            if (!has_WGL_EXT_create_context_es2_profile) {
                ReleaseDC(windowHandle, hDC);
                wglDeleteContext(null, dummyContext);
                wglMakeCurrent(null, currentDc, currentContext);
                throw new IllegalStateException("OpenGL ES API requested but WGL_EXT_create_context_es2_profile is unavailable");
            }
            profile = WGL_CONTEXT_ES2_PROFILE_BIT_EXT;
        }
        if (profile > 0) {
            boolean has_WGL_ARB_create_context_profile = wglExtensionsList.contains("WGL_ARB_create_context_profile");
            if (!has_WGL_ARB_create_context_profile) {
                ReleaseDC(windowHandle, hDC);
                wglDeleteContext(null, dummyContext);
                wglMakeCurrent(null, currentDc, currentContext);
                throw new IllegalStateException("OpenGL profile requested but WGL_ARB_create_context_profile is unavailable");
            }
            attribList.put(WGL_CONTEXT_PROFILE_MASK_ARB).put(profile);
        }
        int contextFlags = 0;
        if (attribs.debug) {
            contextFlags |= WGL_CONTEXT_DEBUG_BIT_ARB;
        }
        if (attribs.forwardCompatible) {
            contextFlags |= WGL_CONTEXT_FORWARD_COMPATIBLE_BIT_ARB;
        }
        if (attribs.robustness) {
            // Check for WGL_ARB_create_context_robustness
            boolean has_WGL_ARB_create_context_robustness = wglExtensions.contains("WGL_ARB_create_context_robustness");
            if (!has_WGL_ARB_create_context_robustness) {
                ReleaseDC(windowHandle, hDC);
                wglDeleteContext(null, dummyContext);
                wglMakeCurrent(null, currentDc, currentContext);
                throw new IllegalStateException("Context with robust buffer access requested but WGL_ARB_create_context_robustness is unavailable");
            }
            contextFlags |= WGL_CONTEXT_ROBUST_ACCESS_BIT_ARB;
            if (attribs.loseContextOnReset) {
                attribList.put(WGL_CONTEXT_RESET_NOTIFICATION_STRATEGY_ARB).put(
                    WGL_LOSE_CONTEXT_ON_RESET_ARB);
                // Note: WGL_NO_RESET_NOTIFICATION_ARB is default behaviour and need not be specified.
            }
            if (attribs.contextResetIsolation) {
                // Check for WGL_ARB_robustness_application_isolation or WGL_ARB_robustness_share_group_isolation
                boolean has_WGL_ARB_robustness_application_isolation = wglExtensions.contains("WGL_ARB_robustness_application_isolation");
                boolean has_WGL_ARB_robustness_share_group_isolation = wglExtensions.contains("WGL_ARB_robustness_share_group_isolation");
                if (!has_WGL_ARB_robustness_application_isolation && !has_WGL_ARB_robustness_share_group_isolation) {
                    ReleaseDC(windowHandle, hDC);
                    wglDeleteContext(null, dummyContext);
                    wglMakeCurrent(null, currentDc, currentContext);
                    throw new IllegalStateException(
                        "Robustness isolation requested but neither WGL_ARB_robustness_application_isolation nor WGL_ARB_robustness_share_group_isolation available");
                }
                contextFlags |= WGL_CONTEXT_RESET_ISOLATION_BIT_ARB;
            }
        }
        if (contextFlags > 0) {
            attribList.put(WGL_CONTEXT_FLAGS_ARB).put(contextFlags);
        }
        if (attribs.contextReleaseBehavior != null) {
            boolean has_WGL_ARB_context_flush_control = wglExtensionsList.contains("WGL_ARB_context_flush_control");
            if (!has_WGL_ARB_context_flush_control) {
                ReleaseDC(windowHandle, hDC);
                wglDeleteContext(null, dummyContext);
                wglMakeCurrent(null, currentDc, currentContext);
                throw new IllegalStateException("Context release behavior requested but WGL_ARB_context_flush_control is unavailable");
            }
            if (attribs.contextReleaseBehavior == GLData.ReleaseBehavior.NONE) {
                attribList.put(WGL_CONTEXT_RELEASE_BEHAVIOR_ARB).put(WGL_CONTEXT_RELEASE_BEHAVIOR_NONE_ARB);
            } else if (attribs.contextReleaseBehavior == GLData.ReleaseBehavior.FLUSH) {
                attribList.put(WGL_CONTEXT_RELEASE_BEHAVIOR_ARB).put(WGL_CONTEXT_RELEASE_BEHAVIOR_FLUSH_ARB);
            }
        }
        attribList.put(0).put(0);
        // Set pixelformat
        success = SetPixelFormat(null, hDC, pixelFormat, pfd);
        if (!success) {
            ReleaseDC(windowHandle, hDC);
            wglDeleteContext(null, dummyContext);
            wglMakeCurrent(null, currentDc, currentContext);
            throw new IllegalStateException("Failed to set pixel format.");
        }
        // And create new context with it
        long newCtx = callPPPP(hDC, attribs.shareContext != null ? attribs.shareContext.context : 0L, attribListAddr, wglCreateContextAttribsARBAddr);
        wglDeleteContext(null, dummyContext);
        if (newCtx == 0L) {
            ReleaseDC(windowHandle, hDC);
            wglMakeCurrent(null, currentDc, currentContext);
            throw new IllegalStateException("Failed to create OpenGL context.");
        }
        // Make context current for next operations
        wglMakeCurrent(null, hDC, newCtx);
        if (attribs.swapInterval != null) {
            boolean has_WGL_EXT_swap_control = wglExtensionsList.contains("WGL_EXT_swap_control");
            if (!has_WGL_EXT_swap_control) {
                ReleaseDC(windowHandle, hDC);
                wglMakeCurrent(null, currentDc, currentContext);
                wglDeleteContext(null, newCtx);
                throw new IllegalStateException("Swap interval requested but WGL_EXT_swap_control is unavailable");
            }
            if (attribs.swapInterval < 0) {
                // Only allowed if WGL_EXT_swap_control_tear is available
                boolean has_WGL_EXT_swap_control_tear = wglExtensionsList.contains("WGL_EXT_swap_control_tear");
                if (!has_WGL_EXT_swap_control_tear) {
                    ReleaseDC(windowHandle, hDC);
                    wglMakeCurrent(null, currentDc, currentContext);
                    wglDeleteContext(null, newCtx);
                    throw new IllegalStateException("Negative swap interval requested but WGL_EXT_swap_control_tear is unavailable");
                }
            }
            long wglSwapIntervalEXTAddr = wglGetProcAddress(null, "wglSwapIntervalEXT");
            if (wglSwapIntervalEXTAddr != 0L) {
                callI(attribs.swapInterval, wglSwapIntervalEXTAddr);
            }
        }
        ReleaseDC(windowHandle, hDC);
        long getInteger = GL.getFunctionProvider().getFunctionAddress("glGetIntegerv");
        long getString = GL.getFunctionProvider().getFunctionAddress("glGetString");
        effective.api = attribs.api;
        if (atLeast30(attribs.majorVersion, attribs.minorVersion)) {
            callPV(GL_MAJOR_VERSION, bufferAddr, getInteger);
            effective.majorVersion = memGetInt(bufferAddr);
            callPV(GL_MINOR_VERSION, bufferAddr, getInteger);
            effective.minorVersion = memGetInt(bufferAddr);
            callPV(GL_CONTEXT_FLAGS, bufferAddr, getInteger);
            int effectiveContextFlags = memGetInt(bufferAddr);
            effective.debug = (effectiveContextFlags & GL_CONTEXT_FLAG_DEBUG_BIT) != 0;
            effective.forwardCompatible = (effectiveContextFlags & GL_CONTEXT_FLAG_FORWARD_COMPATIBLE_BIT) != 0;
            effective.robustness = (effectiveContextFlags & GL_CONTEXT_FLAG_ROBUST_ACCESS_BIT_ARB) != 0;
        } else if (attribs.api == GLData.API.GL) {
            APIVersion version = apiParseVersion(memUTF8(Checks.check(callP(GL_VERSION, getString))));
            effective.majorVersion = version.major;
            effective.minorVersion = version.minor;
        } else if (attribs.api == GLData.API.GLES) {
            APIVersion version = apiParseVersion(memUTF8(Checks.check(callP(GL_VERSION, getString))));
            effective.majorVersion = version.major;
            effective.minorVersion = version.minor;
        }
        if (attribs.api == GLData.API.GL && atLeast32(effective.majorVersion, effective.minorVersion)) {
            callPV(GL_CONTEXT_PROFILE_MASK, bufferAddr, getInteger);
            int effectiveProfileMask = memGetInt(bufferAddr);
            boolean core = (effectiveProfileMask & GL_CONTEXT_CORE_PROFILE_BIT) != 0;
            boolean comp = (effectiveProfileMask & GL_CONTEXT_COMPATIBILITY_PROFILE_BIT) != 0;
            if (comp) {
                effective.profile = GLData.Profile.COMPATIBILITY;
            } else if (core) {
                effective.profile = GLData.Profile.CORE;
            } else {
                effective.profile = null;
            }
        }
        if (attribs.samples >= 1) {
            callPV(GL_SAMPLES_ARB, bufferAddr, getInteger);
            effective.samples = memGetInt(bufferAddr);
            callPV(GL_SAMPLE_BUFFERS_ARB, bufferAddr, getInteger);
            effective.sampleBuffers = memGetInt(bufferAddr);
        }
        // Restore old context
        wglMakeCurrent(null, currentDc, currentContext);
        return newCtx;
    }

    @Override
    public boolean makeCurrent(long context) {
        if (context == 0L) {
            return wglMakeCurrent(null, 0L, 0L);
        }
        long hdc = GetDC(hwnd);
        if (hdc == 0L) {
            return false;
        }
        boolean ret = wglMakeCurrent(null, hdc, context);
        ReleaseDC(hwnd, hdc);
        return ret;
    }

    @Override
    public void swapBuffers() {
        long hdc = GetDC(hwnd);
        if (hdc != 0L) {
            SwapBuffers(null, hdc);
            ReleaseDC(hwnd, hdc);
        }
    }
}
