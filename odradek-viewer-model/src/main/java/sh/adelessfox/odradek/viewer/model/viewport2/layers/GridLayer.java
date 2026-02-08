package sh.adelessfox.odradek.viewer.model.viewport2.layers;

import sh.adelessfox.odradek.math.Matrix4f;
import sh.adelessfox.odradek.math.Vector4f;
import sh.adelessfox.odradek.viewer.model.viewport2.WgpuPanel;
import sh.adelessfox.odradek.viewer.model.viewport2.WgpuViewport;
import sh.adelessfox.wgpuj.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Optional;

public final class GridLayer implements Layer {
    private static final String SHADER = """
        struct UniformsPerFrame {
            view:  mat4x4<f32>,
            proj:  mat4x4<f32>,
            pos:   vec3<f32>,
        }
        
        @group(0) @binding(0) var<uniform> u_frame: UniformsPerFrame;
        
        @vertex
        fn vs_main(@builtin(vertex_index) in_vertex_index: u32) -> @builtin(position) vec4f {
            let x = f32(i32(in_vertex_index) - 1);
            let y = f32(i32(in_vertex_index & 1u) * 2 - 1);
            return vec4<f32>(x, y, 0.0, 1.0);
        }
        
        @fragment
        fn fs_main() -> @location(0) vec4f {
            return vec4f(1.0);
        }
        """;

    private ShaderModule module;
    private RenderPipeline pipeline;

    private Buffer uniformsGpu;
    private ByteBuffer uniformsCpu;
    private BindGroupLayout uniformBindGroupLayout;
    private BindGroup uniformBindGroup;

    @Override
    public void onAttach(WgpuViewport viewport, Device device, Queue queue) {
        createResources(device);
    }

    @Override
    public void onRender(WgpuViewport viewport, Queue queue, RenderPass pass, float delta) {
        var buffer = uniformsCpu.asFloatBuffer();

        // Shared uniforms
        var camera = viewport.getCamera();
        camera.view().get(buffer.slice(0, 16));
        camera.projection().get(buffer.slice(16, 16));
        camera.position().get(buffer.slice(32, 3));
        queue.writeBuffer(uniformsGpu, 0, uniformsCpu);

        pass.setPipeline(pipeline);
        pass.setBindGroup(0, Optional.of(uniformBindGroup));
        pass.draw(3, 1, 0, 0);
    }

    @Override
    public void onDetach() {
        uniformBindGroup.close();
        uniformBindGroupLayout.close();
        uniformsGpu.close();

        pipeline.close();
        module.close();
    }

    private void createResources(Device device) {
        // 0x0000: mat4f view
        // 0x0040: mat4f proj
        // 0x0080: vec3f pos
        // 0x00c0: <0x40 padding>

        int size = 256;
        uniformsGpu = device.createBuffer(BufferDescriptor.builder()
            .size(size)
            .addUsages(BufferUsage.COPY_DST, BufferUsage.UNIFORM)
            .mappedAtCreation(false)
            .build());
        uniformsCpu = ByteBuffer.allocateDirect(size)
            .order(ByteOrder.LITTLE_ENDIAN);

        uniformBindGroupLayout = device.createBindGroupLayout(BindGroupLayoutDescriptor.builder()
            .label("uniform bind group layout")
            .addEntries(BindGroupLayoutEntry.builder()
                .binding(0)
                .addVisibility(ShaderStage.FRAGMENT, ShaderStage.VERTEX)
                .type(BindingType.Buffer.builder()
                    .type(new BufferBindingType.Uniform())
                    .hasDynamicOffset(false)
                    .build())
                .build())
            .build());

        uniformBindGroup = device.createBindGroup(BindGroupDescriptor.builder()
            .label("uniform bind group")
            .layout(uniformBindGroupLayout)
            .addEntries(BindGroupEntry.builder()
                .binding(0)
                .resource(new BindingResource.Buffer(uniformsGpu, 0, Matrix4f.BYTES * 2 + Vector4f.BYTES))
                .build())
            .build());

        module = device.createShaderModule(ShaderModuleDescriptor.builder()
            .label("model layer shader module")
            .source(new ShaderSource.Wgsl(SHADER))
            .build());

        pipeline = createPrimitiveRenderPipeline(
            device,
            module,
            List.of(uniformBindGroupLayout));
    }

    private static RenderPipeline createPrimitiveRenderPipeline(
        Device device,
        ShaderModule shaderModule,
        List<BindGroupLayout> bindGroupLayouts
    ) {
        var layoutDescriptor = PipelineLayoutDescriptor.builder()
            .label("model layer pipeline layout")
            .bindGroupLayouts(bindGroupLayouts)
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
                .depthStencil(DepthStencilState.builder()
                    .format(WgpuPanel.DEPTH_ATTACHMENT_FORMAT)
                    .depthCompare(CompareFunction.LESS)
                    .depthWriteEnabled(true)
                    .stencil(StencilState.builder()
                        .front(StencilFaceState.IGNORE)
                        .back(StencilFaceState.IGNORE)
                        .build())
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
                        .format(WgpuPanel.COLOR_ATTACHMENT_FORMAT)
                        .addWriteMask(ColorWrites.ALL)
                        .build())
                    .build())
                .build();

            return device.createRenderPipeline(pipelineDescriptor);
        }
    }
}
