package sh.adelessfox.odradek.service;

import sh.adelessfox.odradek.data.DataContext;

import java.util.ServiceLoader;
import java.util.stream.Stream;

/** Created once per application and disposed by its shutdown hook. */
public interface ApplicationService {
    interface Provider {
        /** Uses prebound dependencies; provider discovery order must not be relied upon. */
        ApplicationService create(DataContext context);
    }

    static Stream<Provider> providers() {
        return ServiceLoader.load(Provider.class).stream().map(ServiceLoader.Provider::get);
    }

    void dispose();
}
