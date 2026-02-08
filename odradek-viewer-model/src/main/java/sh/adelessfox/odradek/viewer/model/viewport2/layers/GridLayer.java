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
        var<private> pos : array<vec3f, 6> = array<vec3f, 6>(
            vec3( 1,  1, 0), vec3(-1, -1, 0), vec3(-1,  1, 0),
            vec3(-1, -1, 0), vec3( 1,  1, 0), vec3( 1, -1, 0));

        struct UniformsPerFrame {
            view:     mat4x4<f32>,
            view_inv: mat4x4<f32>,
            proj:     mat4x4<f32>,
            proj_inv: mat4x4<f32>,
            pos:      vec3<f32>,
        }
        
        struct VertexOutput {
            @builtin(position) position: vec4f,
            @location(0) near_point: vec3f,
            @location(1) far_point: vec3f
        }
        
        @group(0) @binding(0) var<uniform> u_frame: UniformsPerFrame;
        
        fn unproject_point(in: vec3f) -> vec3f {
            var out = u_frame.view_inv * u_frame.proj_inv * vec4(in, 1.0);
            return out.xyz / out.w;
        }
        
        @vertex
        fn vs_main(@builtin(vertex_index) in_vertex_index: u32) -> VertexOutput {
            var out: VertexOutput;
            out.position = vec4<f32>(pos[in_vertex_index], 1.0);
            out.near_point = unproject_point(vec3(out.position.xy, 0.0));
            out.far_point = unproject_point(vec3(out.position.xy, 1.0));
            return out;
        }
        
        fn grid(pos: vec3f, scale: f32, is_axis: bool) -> vec4f {
            var coord = pos.xy * scale;
            var derivative = fwidth(coord);
            var grid = abs(fract(coord - 0.5) - 0.5) / derivative;
            var min_x = min(derivative.x, 1.0);
            var min_y = min(derivative.y, 1.0);
        
            var color: vec3f;
            if (-min_x < pos.x && pos.x < 0.1 * min_x && is_axis) {
                color = vec3(0.2, 0.8, 0.2);
            } else if (-min_y < pos.y && pos.y < 0.1 * min_y && is_axis) {
                color = vec3(0.9, 0.2, 0.2);
            } else {
                color = vec3(0.2);
            }
        
            var axis = min(grid.x, grid.y);
            var alpha = 1.0 - min(axis, 1.0);
        
            return vec4(color, alpha);
        }
        
        @fragment
        fn fs_main(in: VertexOutput) -> @location(0) vec4f {
            var t = -in.near_point.z / (in.far_point.z - in.near_point.z);
            var pos = in.near_point + t * (in.far_point - in.near_point);
            var col = grid(pos, 1, true) + grid(pos, 10, false); 
            return col * f32(t > 0);
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
        camera.view().invert().get(buffer.slice(16, 16));
        camera.projection().get(buffer.slice(32, 16));
        camera.projection().invert().get(buffer.slice(48, 16));
        camera.position().get(buffer.slice(64, 3));
        queue.writeBuffer(uniformsGpu, 0, uniformsCpu);

        pass.setPipeline(pipeline);
        pass.setBindGroup(0, Optional.of(uniformBindGroup));
        pass.draw(6, 1, 0, 0);
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
        int size = Matrix4f.BYTES * 4 + Vector4f.BYTES;
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
                .resource(new BindingResource.Buffer(uniformsGpu, 0, Matrix4f.BYTES * 4 + Vector4f.BYTES))
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
                    .depthWriteEnabled(false)
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
