package com.rizhik.visuals.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.rizhik.visuals.client.module.Module;
import com.rizhik.visuals.client.module.ModuleManager;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public class WorldFx {
    private static final RenderPipeline PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("rizhikvisuals", "pipeline/world_fx"))
                    .build());

    private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static MappableRingBuffer vertexBuffer;
    private static boolean failed;

    public static int argb(int rgb, double alpha) {
        int a = (int) (Math.max(0, Math.min(1, alpha)) * 255);
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    public static class Quads {
        private final BufferBuilder buffer;
        private final Matrix4fc pose;
        private final Vec3 cam;
        private final float[] px = new float[4], py = new float[4], pz = new float[4];
        private final float[] pr = new float[4], pg = new float[4], pb = new float[4], pa = new float[4];
        private int n;

        Quads(BufferBuilder buffer, Matrix4fc pose, Vec3 cam) {
            this.buffer = buffer;
            this.pose = pose;
            this.cam = cam;
        }

        public void vertex(double x, double y, double z, int argb) {
            px[n] = (float) (x - cam.x);
            py[n] = (float) (y - cam.y);
            pz[n] = (float) (z - cam.z);
            pr[n] = ((argb >> 16) & 255) / 255f;
            pg[n] = ((argb >> 8) & 255) / 255f;
            pb[n] = (argb & 255) / 255f;
            pa[n] = ((argb >>> 24) & 255) / 255f;
            n++;
            if (n == 4) {
                for (int i = 0; i < 4; i++) put(i);
                for (int i = 3; i >= 0; i--) put(i);
                n = 0;
            }
        }

        private void put(int i) {
            buffer.addVertex(pose, px[i], py[i], pz[i]).setColor(pr[i], pg[i], pb[i], pa[i]);
        }
    }

    public static void render(WorldRenderContext context) {
        if (failed) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        boolean any = false;
        for (Module m : ModuleManager.all()) {
            if (m.isEnabled()) {
                any = true;
                break;
            }
        }
        if (!any) return;

        try {
            PoseStack matrices = context.matrices();
            Vec3 cam = context.worldState().cameraRenderState.pos;
            BufferBuilder buffer = new BufferBuilder(ALLOCATOR,
                    PIPELINE.getVertexFormatMode(), PIPELINE.getVertexFormat());
            Quads q = new Quads(buffer, matrices.last().pose(), cam);
            for (Module m : ModuleManager.all()) {
                if (m.isEnabled()) m.renderWorld(q);
            }
            MeshData mesh = buffer.build();
            if (mesh == null) return;
            draw(mc, mesh);
        } catch (RuntimeException e) {
            failed = true;
        }
    }

    private static GpuBuffer upload(MeshData.DrawState drawParameters, VertexFormat format, MeshData built) {
        int size = drawParameters.vertexCount() * format.getVertexSize();

        if (vertexBuffer == null || vertexBuffer.size() < size) {
            if (vertexBuffer != null) vertexBuffer.close();
            vertexBuffer = new MappableRingBuffer(() -> "rizhikvisuals world fx",
                    GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, size);
        }

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        try (GpuBuffer.MappedView view = encoder.mapBuffer(
                vertexBuffer.currentBuffer().slice(0, built.vertexBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(built.vertexBuffer(), view.data());
        }
        return vertexBuffer.currentBuffer();
    }

    private static void draw(Minecraft mc, MeshData built) {
        MeshData.DrawState params = built.drawState();
        VertexFormat format = params.format();
        GpuBuffer vertices = upload(params, format, built);

        GpuBuffer indices;
        VertexFormat.IndexType indexType;
        if (PIPELINE.getVertexFormatMode() == VertexFormat.Mode.QUADS) {
            built.sortQuads(ALLOCATOR, RenderSystem.getProjectionType().vertexSorting());
            indices = PIPELINE.getVertexFormat().uploadImmediateIndexBuffer(built.indexBuffer());
            indexType = built.drawState().indexType();
        } else {
            RenderSystem.AutoStorageIndexBuffer shape = RenderSystem.getSequentialBuffer(PIPELINE.getVertexFormatMode());
            indices = shape.getBuffer(params.indexCount());
            indexType = shape.type();
        }

        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "rizhikvisuals world fx render",
                mc.getMainRenderTarget().getColorTextureView(), OptionalInt.empty(),
                mc.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty())) {
            pass.setPipeline(PIPELINE);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transforms);
            pass.setVertexBuffer(0, vertices);
            pass.setIndexBuffer(indices, indexType);
            pass.drawIndexed(0, 0, params.indexCount(), 1);
        }

        built.close();
        vertexBuffer.rotate();
    }
}
