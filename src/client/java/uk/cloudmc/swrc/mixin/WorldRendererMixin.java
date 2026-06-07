package uk.cloudmc.swrc.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.cloudmc.swrc.SWRC;
import uk.cloudmc.swrc.render.TrackBuilderRenderer;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    private final TrackBuilderRenderer trackBuilderRenderer = new TrackBuilderRenderer();

    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void onRenderLast(
            ObjectAllocator allocator,
            RenderTickCounter tickCounter,
            boolean renderBlockOutline,
            Camera camera,
            Matrix4f positionMatrix,
            Matrix4f basicProjectionMatrix,
            Matrix4f projectionMatrix,
            GpuBufferSlice fogBuffer,
            Vector4f fogColor,
            boolean renderSky,
            CallbackInfo ci
    ) {
        // Get the queue from GameRenderer — this is the correct public accessor in 1.21.11
        OrderedRenderCommandQueueImpl queue =
                MinecraftClient.getInstance().gameRenderer.getEntityRenderCommandQueue();

        MatrixStack matrixStack = new MatrixStack();
        matrixStack.multiplyPositionMatrix(positionMatrix);

        trackBuilderRenderer.onRender(matrixStack, camera, queue);
    }
}