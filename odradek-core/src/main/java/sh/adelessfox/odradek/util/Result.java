package sh.adelessfox.odradek.util;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface Result<T, E> {
    static <T, E> Result<T, E> ok(T value) {
        return new Ok<>(value);
    }

    static <T, E> Result<T, E> error(E error) {
        return new Error<>(error);
    }

    default boolean isOk() {
        return this instanceof Ok<T, E>;
    }

    default boolean isError() {
        return this instanceof Error<T, E>;
    }

    default T unwrap() {
        return switch (this) {
            case Ok<T, E>(var value) -> value;
            case Error<T, E> _ -> throw new NoSuchElementException("No value present");
        };
    }

    default E unwrapError() {
        return switch (this) {
            case Ok<T, E> _ -> throw new NoSuchElementException("No error present");
            case Error<T, E>(var value) -> value;
        };
    }

    default <U> Result<U, E> map(Function<? super T, ? extends U> mapper) {
        return switch (this) {
            case Ok<T, E>(var value) -> Result.ok(mapper.apply(value));
            case Error<T, E>(var error) -> Result.error(error);
        };
    }

    default <U> Result<T, U> mapError(Function<? super E, ? extends U> mapper) {
        return switch (this) {
            case Ok<T, E>(var value) -> Result.ok(value);
            case Error<T, E>(var error) -> Result.error(mapper.apply(error));
        };
    }

    @SuppressWarnings("unchecked")
    default <U> Result<U, E> flatMap(Function<? super T, ? extends Result<? extends U, ? extends E>> mapper) {
        return switch (this) {
            case Ok<T, E>(var value) -> (Result<U, E>) mapper.apply(value);
            case Error<T, E>(var error) -> Result.error(error);
        };
    }

    default <U> U fold(Function<? super T, ? extends U> mapper, Function<? super E, ? extends U> errorMapper) {
        return switch (this) {
            case Ok<T, E>(var value) -> mapper.apply(value);
            case Error<T, E>(var error) -> errorMapper.apply(error);
        };
    }

    default T orElse(T other) {
        return switch (this) {
            case Ok<T, E>(var value) -> value;
            case Error<T, E> _ -> other;
        };
    }

    default T orElseGet(Supplier<? extends T> supplier) {
        return switch (this) {
            case Ok<T, E>(var value) -> value;
            case Error<T, E> _ -> supplier.get();
        };
    }

    record Ok<T, E>(T value) implements Result<T, E> {
        /** @deprecated Use the static factory method {@link Result#ok(Object)} instead. */
        @Deprecated
        public Ok {
            Objects.requireNonNull(value);
        }
    }

    record Error<T, E>(E value) implements Result<T, E> {
        /** @deprecated Use the static factory method {@link Result#error(Object)} instead. */
        @Deprecated
        public Error {
            Objects.requireNonNull(value);
        }
    }
}
