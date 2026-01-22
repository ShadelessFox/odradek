package sh.adelessfox.odradek.util;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

sealed public interface Result<T, E> {
    static <T, E> Result<T, E> ok(T value) {
        return new Ok<>(value);
    }

    static <T, E> Result<T, E> error(E error) {
        return new Error<>(error);
    }

    boolean isOk();

    boolean isError();

    Optional<T> ok();

    Optional<E> error();

    <U> Result<U, E> map(Function<? super T, ? extends U> mapper);

    <U> Result<T, U> mapError(Function<? super E, ? extends U> mapper);

    <U> Result<U, E> flatMap(Function<? super T, ? extends Result<? extends U, ? extends E>> mapper);

    <U> U fold(Function<? super T, ? extends U> mapper, Function<? super E, ? extends U> errorMapper);

    T orElse(T other);

    T orElseGet(Supplier<? extends T> supplier);

    record Ok<T, E>(T value) implements Result<T, E> {
        public Ok {
            Objects.requireNonNull(value);
        }

        @Override
        public boolean isOk() {
            return true;
        }

        @Override
        public boolean isError() {
            return false;
        }

        @Override
        public Optional<T> ok() {
            return Optional.of(value);
        }

        @Override
        public Optional<E> error() {
            return Optional.empty();
        }

        @Override
        public <U> Result<U, E> map(Function<? super T, ? extends U> mapper) {
            return Result.ok(mapper.apply(value));
        }

        @SuppressWarnings("unchecked")
        @Override
        public <U> Result<T, U> mapError(Function<? super E, ? extends U> mapper) {
            return (Result<T, U>) this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <U> Result<U, E> flatMap(Function<? super T, ? extends Result<? extends U, ? extends E>> mapper) {
            return (Result<U, E>) mapper.apply(value);
        }

        @Override
        public <U> U fold(Function<? super T, ? extends U> mapper, Function<? super E, ? extends U> errorMapper) {
            return mapper.apply(value);
        }

        @Override
        public T orElse(T other) {
            return value;
        }

        @Override
        public T orElseGet(Supplier<? extends T> supplier) {
            return value;
        }
    }

    record Error<T, E>(E value) implements Result<T, E> {
        public Error {
            Objects.requireNonNull(value);
        }

        @Override
        public boolean isOk() {
            return false;
        }

        @Override
        public boolean isError() {
            return true;
        }

        @Override
        public Optional<T> ok() {
            return Optional.empty();
        }

        @Override
        public Optional<E> error() {
            return Optional.of(value);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <U> Result<U, E> map(Function<? super T, ? extends U> mapper) {
            return (Result<U, E>) this;
        }

        @Override
        public <U> Result<T, U> mapError(Function<? super E, ? extends U> mapper) {
            return Result.error(mapper.apply(value));
        }

        @SuppressWarnings("unchecked")
        @Override
        public <U> Result<U, E> flatMap(Function<? super T, ? extends Result<? extends U, ? extends E>> mapper) {
            return (Result<U, E>) this;
        }

        @Override
        public <U> U fold(Function<? super T, ? extends U> mapper, Function<? super E, ? extends U> errorMapper) {
            return errorMapper.apply(value);
        }

        @Override
        public T orElse(T other) {
            return other;
        }

        @Override
        public T orElseGet(Supplier<? extends T> supplier) {
            return supplier.get();
        }
    }
}
