package sh.adelessfox.odradek.viewer.model.viewport2.layers;

import sh.adelessfox.odradek.geometry.Accessor;
import sh.adelessfox.odradek.geometry.Primitive;
import sh.adelessfox.odradek.geometry.Semantic;
import sh.adelessfox.odradek.math.Matrix4f;
import sh.adelessfox.odradek.math.Vector3f;
import sh.adelessfox.odradek.scene.Node;
import sh.adelessfox.odradek.viewer.model.viewport2.Viewport2;
import sh.adelessfox.wgpuj.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

public final class MeshLayer implements Layer {
    private static final String SHADER = """
        struct Uniforms {
            model: mat4x4<f32>,
            view:  mat4x4<f32>,
            proj:  mat4x4<f32>,
            pos:   vec3<f32>,
        }
        
        @group(0) @binding(0) var<uniform> u: Uniforms;
        
        struct VertexInput {
            @location(0) position: vec3f,
            @location(1) normal: vec3f
        }
        
        struct VertexOutput {
            @builtin(position) position: vec4f,
            @location(0) model_position: vec3f,
        }
        
        @vertex
        fn vs_main(in: VertexInput) -> VertexOutput {
            var out: VertexOutput;
            out.position = u.proj * u.view * u.model * vec4f(in.position, 1.0);
            out.model_position = (u.model * vec4f(in.position, 1.0)).xyz;
            return out;
        }
        
        @fragment
        fn fs_main(in: VertexOutput) -> @location(0) vec4f {
            var normal = normalize(cross(dpdx(in.model_position), dpdy(in.model_position)));
            var view = normalize(u.pos - in.model_position);
            var color = vec3f(abs(dot(view, normal)));
        
            return vec4f(color, 1.0);
        }
        """;

    private ShaderModule module;
    private RenderPipeline pipeline;

    private Buffer uniformsGpu;
    private ByteBuffer uniformsCpu;
    private BindGroupLayout uniformBindGroupLayout;
    private BindGroup uniformBindGroup;

    private final List<GpuNode> nodes = new ArrayList<>();

    @Override
    public void onAttach(Viewport2 viewport, Device device, Queue queue) {
        module = device.createShaderModule(ShaderModuleDescriptor.builder()
            .label("model layer shader module")
            .source(new ShaderSource.Wgsl(SHADER))
            .build());

        int size = Matrix4f.BYTES * 3 + Vector3f.BYTES + 4 /* alignment */;
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
                .resource(new BindingResource.Buffer(new BufferBinding(uniformsGpu, 0, OptionalLong.empty())))
                .build())
            .build());

        pipeline = createPrimitiveRenderPipeline(device, module, uniformBindGroupLayout, TextureFormat.RGBA8_UNORM);

        viewport.getScene().accept((node, transform) -> {
            node.mesh().ifPresent(_ -> {
                nodes.add(uploadNode(device, queue, node, transform));
            });
            return true;
        });
    }

    @Override
    public void onRender(Viewport2 viewport, Queue queue, RenderPass pass, float delta) {
        var buffer = uniformsCpu.asFloatBuffer();
        var view = buffer.slice(16, 16);
        var proj = buffer.slice(32, 16);
        var pos = buffer.slice(48, 3);

        var camera = viewport.getCamera();
        camera.view().get(view);
        camera.projection().get(proj);
        camera.position().get(pos);

        pass.setPipeline(pipeline);
        pass.setBindGroup(0, Optional.of(uniformBindGroup));

        for (GpuNode node : nodes) {
            node.transform().get(buffer.slice(0, 16));
            queue.writeBuffer(uniformsGpu, 0, uniformsCpu);

            for (GpuPrimitive primitive : node.primitives()) {
                pass.setIndexBuffer(primitive.indices(), IndexFormat.UINT16, 0, primitive.indices().getSize());
                pass.setVertexBuffer(0, Optional.of(primitive.positions()), 0, primitive.positions().getSize());
                pass.setVertexBuffer(1, Optional.of(primitive.normals()), 0, primitive.normals().getSize());
                pass.drawIndexed(primitive.count(), 1, 0, 0, 0);
                break;
            }

            break;
        }
    }

    @Override
    public void onDetach() {
        uniformBindGroup.close();
        uniformBindGroupLayout.close();
        uniformsGpu.close();
        pipeline.close();
        module.close();
    }

    private static RenderPipeline createPrimitiveRenderPipeline(
        Device device,
        ShaderModule shaderModule,
        BindGroupLayout uniformBindGroup,
        TextureFormat fragmentFormat
    ) {
        var layoutDescriptor = PipelineLayoutDescriptor.builder()
            .label("model layer pipeline layout")
            .addBindGroupLayouts(uniformBindGroup)
            .build();

        try (var layout = device.createPipelineLayout(layoutDescriptor)) {
            var pipelineDescriptor = RenderPipelineDescriptor.builder()
                .label("model layer render pipeline")
                .layout(layout)
                .vertex(VertexState.builder()
                    .module(shaderModule)
                    .entryPoint("vs_main")
                    .addBuffers(VertexBufferLayout.builder()
                        .stepMode(VertexStepMode.VERTEX)
                        .arrayStride(12)
                        .addAttributes(new VertexAttribute(VertexFormat.FLOAT_32x3, 0, 0))  // position
                        .build())
                    .addBuffers(VertexBufferLayout.builder()
                        .stepMode(VertexStepMode.VERTEX)
                        .arrayStride(12)
                        .addAttributes(new VertexAttribute(VertexFormat.FLOAT_32x3, 0, 1))  // normals
                        .build())
                    .build())
                .primitive(PrimitiveState.builder()
                    .topology(PrimitiveTopology.TRIANGLE_LIST)
                    .frontFace(FrontFace.CCW)
                    .build())
                .depthStencil(DepthStencilState.builder()
                    .format(TextureFormat.DEPTH24_PLUS)
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
                        .format(fragmentFormat)
                        .addWriteMask(ColorWrites.ALL)
                        .build())
                    .build())
                .build();

            return device.createRenderPipeline(pipelineDescriptor);
        }
    }

    private static GpuNode uploadNode(Device device, Queue queue, Node node, Matrix4f transform) {
        var primitives = node.mesh().stream()
            .flatMap(mesh -> mesh.primitives().stream())
            .map(p -> uploadPrimitive(device, queue, p))
            .toList();

        return new GpuNode(primitives, node, transform);
    }

    private static GpuPrimitive uploadPrimitive(Device device, Queue queue, Primitive primitive) {
        var vertices = primitive.vertices();
        var positions = createVertexBuffer(device, queue, vertices.get(Semantic.POSITION));
        var normals = createVertexBuffer(device, queue, vertices.get(Semantic.NORMAL));
        var indices = createIndexBuffer(device, queue, primitive.indices());

        return new GpuPrimitive(primitive.indices().count(), positions, normals, indices);
    }

    private static Buffer createVertexBuffer(Device device, Queue queue, Accessor accessor) {
        var view = accessor.asFloatView();
        var bufferCpu = ByteBuffer
            .allocate(accessor.count() * accessor.componentCount() * Float.BYTES + 3 & ~3)
            .order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < accessor.count(); i++) {
            for (int j = 0; j < accessor.componentCount(); j++) {
                bufferCpu.putFloat(view.get(i, j));
            }
        }

        bufferCpu.position(0);

        var bufferGpu = device.createBuffer(BufferDescriptor.builder()
            .size(bufferCpu.remaining())
            .addUsages(BufferUsage.COPY_DST, BufferUsage.VERTEX)
            .mappedAtCreation(false)
            .build());

        queue.writeBuffer(bufferGpu, 0, bufferCpu);

        return bufferGpu;
    }

    private static Buffer createIndexBuffer(Device device, Queue queue, Accessor accessor) {
        var view = accessor.asShortView();
        var bufferCpu = ByteBuffer
            .allocate(accessor.count() * Short.BYTES + 3 & ~3)
            .order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < accessor.count(); i++) {
            bufferCpu.putShort(view.get(i, 0));
        }

        bufferCpu.position(0);

        var bufferGpu = device.createBuffer(BufferDescriptor.builder()
            .size(bufferCpu.remaining())
            .addUsages(BufferUsage.COPY_DST, BufferUsage.INDEX)
            .mappedAtCreation(false)
            .build());

        queue.writeBuffer(bufferGpu, 0, bufferCpu);

        return bufferGpu;
    }

    private record GpuNode(List<GpuPrimitive> primitives, Node node, Matrix4f transform) {
        private GpuNode {
            primitives = List.copyOf(primitives);
        }
    }

    private record GpuPrimitive(int count, Buffer positions, Buffer normals, Buffer indices) {
    }
}
