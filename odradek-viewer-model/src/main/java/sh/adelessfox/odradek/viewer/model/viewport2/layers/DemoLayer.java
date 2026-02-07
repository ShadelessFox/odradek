package sh.adelessfox.odradek.viewer.model.viewport2.layers;

import com.formdev.flatlaf.util.HSLColor;
import sh.adelessfox.odradek.math.Matrix4f;
import sh.adelessfox.odradek.math.Vector3f;
import sh.adelessfox.odradek.viewer.model.viewport2.Viewport2;
import sh.adelessfox.wgpuj.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Optional;
import java.util.OptionalLong;

public final class DemoLayer implements Layer {
    private static final String SHADER = """
        struct Uniforms {
            model: mat4x4<f32>,
            view:  mat4x4<f32>,
            proj:  mat4x4<f32>,
            pos:   vec3<f32>,
        }
        
        @group(0) @binding(0) var<uniform> u: Uniforms;
        
        @vertex
        fn vs_main(@builtin(vertex_index) in_vertex_index: u32) -> @builtin(position) vec4<f32> {
            // return u.proj * u.view * u.model * vec4<f32>(pos);
        
            let x = f32(i32(in_vertex_index) - 1);
            let y = f32(i32(in_vertex_index & 1u) * 2 - 1);
            return vec4<f32>(x, y, 0.0, 1.0);
        }
        
        @fragment
        fn fs_main() -> @location(0) vec4<f32> {
            return vec4<f32>(u.pos, 1.0);
        }
        """;

    private ShaderModule module;
    private RenderPipeline pipeline;

    private Buffer gpuUniforms;
    private ByteBuffer cpuUniforms;
    private BindGroupLayout bindGroupLayout;
    private BindGroup bindGroup;

    private float hue;

    @Override
    public void onAttach(Viewport2 viewport, Device device, Queue queue) {
        module = device.createShaderModule(ShaderModuleDescriptor.builder()
            .label("model layer shader module")
            .source(new ShaderSource.Wgsl(SHADER))
            .build());

        int size = Matrix4f.BYTES * 3 + Vector3f.BYTES + 4 /* alignment */;
        gpuUniforms = device.createBuffer(BufferDescriptor.builder()
            .size(size)
            .addUsages(BufferUsage.COPY_DST, BufferUsage.UNIFORM)
            .mappedAtCreation(false)
            .build());
        cpuUniforms = ByteBuffer.allocateDirect(size)
            .order(ByteOrder.LITTLE_ENDIAN);

        bindGroupLayout = device.createBindGroupLayout(BindGroupLayoutDescriptor.builder()
            .label("mesh bind group layout")
            .addEntries(BindGroupLayoutEntry.builder()
                .binding(0)
                .addVisibility(ShaderStage.FRAGMENT, ShaderStage.VERTEX)
                .type(BindingType.Buffer.builder()
                    .type(new BufferBindingType.Uniform())
                    .hasDynamicOffset(false)
                    .build())
                .build())
            .build());

        bindGroup = device.createBindGroup(BindGroupDescriptor.builder()
            .label("mesh bind group")
            .layout(bindGroupLayout)
            .addEntries(BindGroupEntry.builder()
                .binding(0)
                .resource(new BindingResource.Buffer(new BufferBinding(gpuUniforms, 0, OptionalLong.empty())))
                .build())
            .build());

        pipeline = createRenderPipeline(device, module, bindGroupLayout, TextureFormat.RGBA8_UNORM);
    }

    @Override
    public void onRender(Viewport2 viewport, Queue queue, RenderPass pass, float delta) {
        var color = HSLColor.toRGB(hue, 50, 50);
        cpuUniforms.putFloat(Matrix4f.BYTES * 3 + Float.BYTES * 0, color.getRed() / 255.0f);
        cpuUniforms.putFloat(Matrix4f.BYTES * 3 + Float.BYTES * 1, color.getGreen() / 255.0f);
        cpuUniforms.putFloat(Matrix4f.BYTES * 3 + Float.BYTES * 2, color.getBlue() / 255.0f);
        hue = (hue + delta * 50) % 360;

        queue.writeBuffer(gpuUniforms, 0, cpuUniforms);
        pass.setPipeline(pipeline);
        pass.setBindGroup(0, Optional.of(bindGroup));
        pass.draw(3, 1, 0, 0);
    }

    @Override
    public void onDetach() {
        bindGroup.close();
        bindGroupLayout.close();
        gpuUniforms.close();
        pipeline.close();
        module.close();
    }

    private static RenderPipeline createRenderPipeline(
        Device device,
        ShaderModule shaderModule,
        BindGroupLayout bindGroupLayout,
        TextureFormat format
    ) {
        var layoutDescriptor = PipelineLayoutDescriptor.builder()
            .label("model layer pipeline layout")
            .addBindGroupLayouts(bindGroupLayout)
            .build();

        try (var layout = device.createPipelineLayout(layoutDescriptor)) {
            var pipelineDescriptor = RenderPipelineDescriptor.builder()
                .label("model layer render pipeline")
                .layout(layout)
                .vertex(VertexState.builder()
                    .module(shaderModule)
                    .entryPoint("vs_main")
                    .build())
                .primitive(PrimitiveState.builder()
                    .topology(PrimitiveTopology.TRIANGLE_LIST)
                    .frontFace(FrontFace.CCW)
                    .build())
                .multisample(MultisampleState.builder()
                    .count(1)
                    .mask(0xFFFFFFFF)
                    .alphaToCoverageEnabled(false)
                    .build())
                .fragment(FragmentState.builder()
                    .module(shaderModule)
                    .entryPoint("fs_main")
                    .addTargets(ColorTargetState.builder()
                        .format(format)
                        .addWriteMask(ColorWrites.ALL)
                        .build())
                    .build())
                .build();

            return device.createRenderPipeline(pipelineDescriptor);
        }
    }
}
