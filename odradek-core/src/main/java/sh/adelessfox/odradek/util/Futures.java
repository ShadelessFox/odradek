package sh.adelessfox.odradek.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public final class Futures {
    private Futures() {
    }

    public static <T> CompletableFuture<T> submit(Callable<T> callable) {
        return submit0(callable, CompletableFuture::defaultExecutor);
    }

    public static <T> CompletableFuture<T> submit(Callable<T> callable, Executor executor) {
        return submit0(callable, _ -> executor);
    }

    private static <T> CompletableFuture<T> submit0(Callable<T> callable, Function<CompletableFuture<T>, Executor> supplier) {
        var future = new CompletableFuture<T>();
        var executor = supplier.apply(future);
        executor.execute(() -> {
            try {
                future.complete(callable.call());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }
}
