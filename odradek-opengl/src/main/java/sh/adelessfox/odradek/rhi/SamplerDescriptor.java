package sh.adelessfox.odradek.rhi;

public record SamplerDescriptor(
    AddressMode addressModeU,
    AddressMode addressModeV,
    FilterMode magFilter,
    FilterMode minFilter
) {
}
