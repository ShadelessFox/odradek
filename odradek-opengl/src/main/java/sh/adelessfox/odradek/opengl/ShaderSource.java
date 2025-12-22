package sh.adelessfox.odradek.opengl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public record ShaderSource(String name, String source) {
    public static ShaderSource read(URL url) throws IOException {
        Objects.requireNonNull(url, "url");
        try (InputStream stream = url.openStream()) {
            return read(url.getFile(), stream);
        }
    }

    public static ShaderSource read(String name, InputStream stream) throws IOException {
        String source = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        return new ShaderSource(name, source);
    }
}
