package sh.adelessfox.odradek.app.ui.component.onboarding;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

record OnboardingPage(String label, String title, List<String> body) {
    OnboardingPage {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(title, "title");
        body = List.copyOf(body);
    }

    static List<OnboardingPage> load() {
        var resource = OnboardingPage.class.getResourceAsStream("pages.json");
        if (resource == null) {
            throw new IllegalStateException("Missing onboarding resource: pages.json");
        }
        try (var reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
            var pages = new Gson().fromJson(reader, OnboardingPage[].class);
            if (pages == null || pages.length == 0) {
                throw new IllegalStateException("Onboarding resource pages.json must contain at least one page");
            }
            return List.of(pages);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load onboarding resource: pages.json", e);
        }
    }
}
