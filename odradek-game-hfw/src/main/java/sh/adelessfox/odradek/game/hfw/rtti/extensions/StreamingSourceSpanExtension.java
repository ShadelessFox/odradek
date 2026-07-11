package sh.adelessfox.odradek.game.hfw.rtti.extensions;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;

public interface StreamingSourceSpanExtension {
    default int fileIndex() {
        var span = (HFW.StreamingSourceSpan) this;
        return span.fileIndexAndIsPatch() & 0x7fffffff;
    }

    default void fileIndex(int value) {
        var span = (HFW.StreamingSourceSpan) this;
        span.fileIndexAndIsPatch((span.fileIndexAndIsPatch() & 0x80000000) | (value & 0x7fffffff));
    }

    default boolean isPatch() {
        var span = (HFW.StreamingSourceSpan) this;
        return (span.fileIndexAndIsPatch() & 0x80000000) != 0;
    }

    default void isPatch(boolean value) {
        var span = (HFW.StreamingSourceSpan) this;
        span.fileIndexAndIsPatch((span.fileIndexAndIsPatch() & 0x7fffffff) | (value ? 0x80000000 : 0));
    }

    default long end() {
        var span = (HFW.StreamingSourceSpan) this;
        return (long) span.offset() + span.length();
    }

    default boolean contains(int offset, int length) {
        var span = (HFW.StreamingSourceSpan) this;
        return span.offset() <= offset && offset + length <= span.offset() + span.length();
    }
}
