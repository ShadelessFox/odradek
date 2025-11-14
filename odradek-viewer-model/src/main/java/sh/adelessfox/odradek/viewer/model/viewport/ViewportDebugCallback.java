package sh.adelessfox.odradek.viewer.model.viewport;

import org.lwjgl.opengl.GLDebugMessageCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static org.lwjgl.opengl.GL43.*;

final class ViewportDebugCallback extends GLDebugMessageCallback {
    private static final Logger log = LoggerFactory.getLogger(ViewportDebugCallback.class);

    @Override
    public void invoke(int source, int type, int id, int severity, int length, long message, long userParam) {
        var sourceString = switch (source) {
            case GL_DEBUG_SOURCE_API -> "API";
            case GL_DEBUG_SOURCE_WINDOW_SYSTEM -> "Window System";
            case GL_DEBUG_SOURCE_SHADER_COMPILER -> "Shader Compiler";
            case GL_DEBUG_SOURCE_THIRD_PARTY -> "Third-Party";
            case GL_DEBUG_SOURCE_APPLICATION -> "Application";
            case GL_DEBUG_SOURCE_OTHER -> "Other";
            default -> throw new IllegalStateException("Unexpected source value: " + source);
        };

        var typeString = switch (type) {
            case GL_DEBUG_TYPE_ERROR -> "Error";
            case GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR -> "Deprecated Behavior";
            case GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR -> "Undefined Behavior";
            case GL_DEBUG_TYPE_PORTABILITY -> "Portability";
            case GL_DEBUG_TYPE_PERFORMANCE -> "Performance";
            case GL_DEBUG_TYPE_MARKER -> "Marker";
            case GL_DEBUG_TYPE_PUSH_GROUP -> "Push Group";
            case GL_DEBUG_TYPE_POP_GROUP -> "Pop Group";
            case GL_DEBUG_TYPE_OTHER -> "Other";
            default -> throw new IllegalStateException("Unexpected type value: " + type);
        };

        var builder = switch (severity) {
            case GL_DEBUG_SEVERITY_LOW -> log.atInfo(); // info
            case GL_DEBUG_SEVERITY_MEDIUM -> log.atWarn(); // warning
            case GL_DEBUG_SEVERITY_HIGH -> log.atError(); // error
            case GL_DEBUG_SEVERITY_NOTIFICATION -> log.atTrace(); // trace
            default -> throw new IllegalStateException("Unexpected severity value: " + severity);
        };

        if (severity == GL_DEBUG_SEVERITY_HIGH) {
            var exception = new Exception();
            var trace = exception.getStackTrace();

            // Strip the callback frame
            exception.setStackTrace(Arrays.copyOfRange(trace, 2, trace.length));

            builder = builder.setCause(exception);
        }

        builder.log("[source: {}, type: {}] {}", sourceString, typeString, getMessage(length, message));
    }
}
