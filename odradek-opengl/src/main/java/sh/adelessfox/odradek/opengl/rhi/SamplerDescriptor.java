package sh.adelessfox.odradek.opengl.rhi;

public record SamplerDescriptor(
    AddressMode addressModeU,
    AddressMode addressModeV,
    FilterMode magFilter,
    FilterMode minFilter
) {
}
