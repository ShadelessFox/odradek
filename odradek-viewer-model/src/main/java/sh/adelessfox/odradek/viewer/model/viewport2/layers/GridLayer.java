package sh.adelessfox.odradek.viewer.model.viewport2.layers;

import sh.adelessfox.odradek.math.Matrix4f;
import sh.adelessfox.odradek.math.Vector4f;
import sh.adelessfox.odradek.viewer.model.viewport2.WgpuPanel;
import sh.adelessfox.odradek.viewer.model.viewport2.WgpuViewport;
import sh.adelessfox.wgpuj.*;
import sh.adelessfox.wgpuj.objects.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Optional;

public final class GridLayer implements Layer {
    // language=WGSL
    // language=WGSL
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
        
        struct FragmentOutput {
            @builtin(frag_depth) depth: f32,
            @location(0) color: vec4f
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
        
        fn depth(clip_space_pos: vec4f) -> f32 {
            return clip_space_pos.z / clip_space_pos.w;
        }
        
        fn fade(clip_space_pos: vec4f) -> f32 {
            const near = 0.01;
            const far = 100.0;
        
            var clip_depth = (clip_space_pos.z / clip_space_pos.w) * 2.0 - 1.0;
            var linear_depth = (2.0 * near * far) / (far + near - clip_depth * (far - near));
            return linear_depth / far;
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
        fn fs_main(in: VertexOutput) -> FragmentOutput {
            var t = -in.near_point.z / (in.far_point.z - in.near_point.z);
            var frag_pos = in.near_point + t * (in.far_point - in.near_point);
            var clip_pos = u_frame.proj * u_frame.view * vec4(frag_pos, 1.0);
        
            var color = grid(frag_pos, 1, true) + grid(frag_pos, 10, false) * 0.25;
            color *= max(0.0, 1.0 - fade(clip_pos)); // fade
            color *= f32(t > 0); // discard what's behind the camera
        
            var out: FragmentOutput;
            out.color = color;
            out.depth = depth(clip_pos);
            return out;
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
        uniformsGpu = device.createBuffer(ImmutableBufferDescriptor.builder()
            .size(size)
            .addUsages(BufferUsage.COPY_DST, BufferUsage.UNIFORM)
            .build());
        uniformsCpu = ByteBuffer.allocateDirect(size)
            .order(ByteOrder.LITTLE_ENDIAN);

        uniformBindGroupLayout = device.createBindGroupLayout(ImmutableBindGroupLayoutDescriptor.builder()
            .addEntries(ImmutableBindGroupLayoutEntry.builder()
                .binding(0)
                .addVisibility(ShaderStage.FRAGMENT, ShaderStage.VERTEX)
                .type(ImmutableBindingType.Buffer.builder()
                    .type(new BufferBindingType.Uniform())
                    .hasDynamicOffset(false)
                    .build())
                .build())
            .build());

        uniformBindGroup = device.createBindGroup(ImmutableBindGroupDescriptor.builder()
            .layout(uniformBindGroupLayout)
            .addEntries(ImmutableBindGroupEntry.builder()
                .binding(0)
                .resource(ImmutableBindingResource.Buffer.builder()
                    .buffer(uniformsGpu)
                    .size(Matrix4f.BYTES * 4 + Vector4f.BYTES)
                    .build())
                .build())
            .build());

        module = device.createShaderModule(ImmutableShaderModuleDescriptor.builder()
            .source(ImmutableShaderSource.Wgsl.of(SHADER))
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
        var layoutDescriptor = ImmutablePipelineLayoutDescriptor.builder()
            .bindGroupLayouts(bindGroupLayouts)
            .build();

        try (var layout = device.createPipelineLayout(layoutDescriptor)) {
            var pipelineDescriptor = ImmutableRenderPipelineDescriptor.builder()
                .layout(layout)
                .vertex(ImmutableVertexState.builder()
                    .module(shaderModule)
                    .entryPoint("vs_main")
                    .build())
                .depthStencil(ImmutableDepthStencilState.builder()
                    .format(WgpuPanel.DEPTH_ATTACHMENT_FORMAT)
                    .depthCompare(CompareFunction.LESS)
                    .depthWriteEnabled(true)
                    .build())
                .fragment(ImmutableFragmentState.builder()
                    .module(shaderModule)
                    .entryPoint("fs_main")
                    .addTargets(ImmutableColorTargetState.builder()
                        .format(WgpuPanel.COLOR_ATTACHMENT_FORMAT)
                        .blend(ImmutableBlendState.builder()
                            .color(ImmutableBlendComponent.builder()
                                .srcFactor(BlendFactor.SRC_ALPHA)
                                .dstFactor(BlendFactor.ONE_MINUS_SRC_ALPHA)
                                .build())
                            .alpha(ImmutableBlendComponent.builder()
                                .srcFactor(BlendFactor.SRC_ALPHA)
                                .dstFactor(BlendFactor.ONE_MINUS_SRC_ALPHA)
                                .build())
                            .build())
                        .addWriteMask(ColorWrites.ALL)
                        .build())
                    .build())
                .build();

            return device.createRenderPipeline(pipelineDescriptor);
        }
    }
}
