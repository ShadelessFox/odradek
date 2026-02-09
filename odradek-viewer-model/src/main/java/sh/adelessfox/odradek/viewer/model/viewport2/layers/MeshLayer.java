package sh.adelessfox.odradek.viewer.model.viewport2.layers;

import sh.adelessfox.odradek.geometry.Accessor;
import sh.adelessfox.odradek.geometry.Mesh;
import sh.adelessfox.odradek.geometry.Primitive;
import sh.adelessfox.odradek.geometry.Semantic;
import sh.adelessfox.odradek.math.Matrix4f;
import sh.adelessfox.odradek.math.Vector4f;
import sh.adelessfox.odradek.scene.Scene;
import sh.adelessfox.odradek.viewer.model.viewport2.WgpuPanel;
import sh.adelessfox.odradek.viewer.model.viewport2.WgpuViewport;
import sh.adelessfox.wgpuj.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MeshLayer implements Layer {
    //language=WGSL
    private static final String SHADER = """
        struct UniformsPerFrame {
            view:  mat4x4<f32>,
            proj:  mat4x4<f32>,
            pos:   vec3<f32>,
        }
        
        struct UniformsPerModel {
            model: mat4x4<f32>
        }
        
        @group(0) @binding(0) var<uniform> u_frame: UniformsPerFrame;
        @group(0) @binding(1) var<uniform> u_model: UniformsPerModel;
        
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
            out.position = u_frame.proj * u_frame.view * u_model.model * vec4f(in.position, 1.0);
            out.model_position = (u_model.model * vec4f(in.position, 1.0)).xyz;
            return out;
        }
        
        @fragment
        fn fs_main(in: VertexOutput) -> @location(0) vec4f {
            var normal = normalize(cross(dpdx(in.model_position), dpdy(in.model_position)));
            var view = normalize(u_frame.pos - in.model_position);
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

    private final List<GpuMesh> meshes = new ArrayList<>();

    @Override
    public void onAttach(WgpuViewport viewport, Device device, Queue queue) {
        uploadScene(viewport.getScene(), device, queue);
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

        // Per-mesh uniforms
        for (int i = 0; i < meshes.size(); i++) {
            var node = meshes.get(i);
            node.transform().get(buffer.slice(64 * (i + 1), 16));
        }

        queue.writeBuffer(uniformsGpu, 0, uniformsCpu);

        pass.setPipeline(pipeline);
        for (int i = 0; i < meshes.size(); i++) {
            pass.setBindGroup(0, Optional.of(uniformBindGroup), new int[]{256 * i});
            for (GpuPrimitive primitive : meshes.get(i).primitives()) {
                pass.setIndexBuffer(primitive.indices(), IndexFormat.UINT16, 0, primitive.indices().getSize());
                pass.setVertexBuffer(0, Optional.of(primitive.positions()), 0, primitive.positions().getSize());
                pass.setVertexBuffer(1, Optional.of(primitive.normals()), 0, primitive.normals().getSize());
                pass.drawIndexed(primitive.count(), 1, 0, 0, 0);
            }
        }
    }

    @Override
    public void onDetach() {
        meshes.forEach(GpuMesh::dispose);
        meshes.clear();

        uniformBindGroup.close();
        uniformBindGroupLayout.close();
        uniformsGpu.close();

        pipeline.close();
        module.close();
    }

    private void uploadScene(Scene scene, Device device, Queue queue) {
        scene.accept((node, transform) -> {
            node.mesh()
                .map(mesh -> uploadMesh(device, queue, mesh, transform))
                .ifPresent(meshes::add);
            return true;
        });
    }

    private void createResources(Device device) {
        // 0x0000: mat4f view
        // 0x0040: mat4f proj
        // 0x0080: vec3f pos
        // 0x00c0: <0x40 padding>
        // 0x0100: mat4f model[0]
        // 0x0140: <0xc0 padding>
        // 0x0200: mat4f model[1]

        int size = 256 + 256 * meshes.size();
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
            .addEntries(BindGroupLayoutEntry.builder()
                .binding(1)
                .addVisibility(ShaderStage.FRAGMENT, ShaderStage.VERTEX)
                .type(BindingType.Buffer.builder()
                    .type(new BufferBindingType.Uniform())
                    .hasDynamicOffset(true)
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
            .addEntries(BindGroupEntry.builder()
                .binding(1)
                .resource(new BindingResource.Buffer(uniformsGpu, 256, Matrix4f.BYTES))
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

    private static GpuMesh uploadMesh(Device device, Queue queue, Mesh mesh, Matrix4f transform) {
        var primitives = mesh.primitives().stream()
            .map(p -> uploadPrimitive(device, queue, p))
            .toList();

        return new GpuMesh(primitives, transform);
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

    private record GpuMesh(List<GpuPrimitive> primitives, Matrix4f transform) {
        private GpuMesh {
            primitives = List.copyOf(primitives);
        }

        void dispose() {
            for (GpuPrimitive primitive : primitives) {
                primitive.dispose();
            }
        }
    }

    private record GpuPrimitive(int count, Buffer positions, Buffer normals, Buffer indices) {
        void dispose() {
            positions.close();
            normals.close();
            indices.close();
        }
    }
}
