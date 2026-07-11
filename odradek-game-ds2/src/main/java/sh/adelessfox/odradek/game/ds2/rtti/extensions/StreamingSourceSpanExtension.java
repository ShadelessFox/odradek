package sh.adelessfox.odradek.game.ds2.rtti.extensions;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;

public interface StreamingSourceSpanExtension {
    default int fileIndex() {
        var span = (DS2.StreamingSourceSpan) this;
        return span.fileIndexAndIsPatch() & 0x7fffffff;
    }

    default void fileIndex(int value) {
        var span = (DS2.StreamingSourceSpan) this;
        span.fileIndexAndIsPatch((span.fileIndexAndIsPatch() & 0x80000000) | (value & 0x7fffffff));
    }

    default boolean isPatch() {
        var span = (DS2.StreamingSourceSpan) this;
        return (span.fileIndexAndIsPatch() & 0x80000000) != 0;
    }

    default void isPatch(boolean value) {
        var span = (DS2.StreamingSourceSpan) this;
        span.fileIndexAndIsPatch((span.fileIndexAndIsPatch() & 0x7fffffff) | (value ? 0x80000000 : 0));
    }

    default long end() {
        var span = (DS2.StreamingSourceSpan) this;
        return (long) span.offset() + span.length();
    }

    default boolean contains(int offset, int length) {
        var span = (DS2.StreamingSourceSpan) this;
        return span.offset() <= offset && offset + length <= span.offset() + span.length();
    }
}
