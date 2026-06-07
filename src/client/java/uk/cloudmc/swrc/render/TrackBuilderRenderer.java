package uk.cloudmc.swrc.render;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.*;
import net.minecraft.client.render.command.RenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import uk.cloudmc.swrc.SWRC;
import uk.cloudmc.swrc.track.Checkpoint;
import uk.cloudmc.swrc.track.Trap;
import uk.cloudmc.swrc.track.TrackBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;

import java.util.ArrayList;
import java.util.OptionalDouble;

public class TrackBuilderRenderer {

    private static final RenderLayer DEBUG_LINE_LAYER = RenderLayer.of(
            "swrc_debug_lines",
            RenderSetup.builder(RenderPipelines.LINES)
                    .build()
    );

    public void renderCheckpoint(MatrixStack matrixStack, Camera camera, Vec3d left, Vec3d right, int color, RenderCommandQueue queue) {
        Vector3f lrDelta = right.subtract(left).toVector3f();
        Vector3f dir = new Vector3f(lrDelta.z, lrDelta.y, -lrDelta.x).normalize();
        int colorFaded = color & 0xFFFFFF00;

        Vector3f fLeft  = left.subtract(camera.getCameraPos()).toVector3f();
        Vector3f fRight = right.subtract(camera.getCameraPos()).toVector3f();

        queue.submitCustom(matrixStack, DEBUG_LINE_LAYER, (entry, consumer) -> {
            Matrix4f m = entry.getPositionMatrix();
            // For each pair of vertices, the normal is the direction from point A to point B
            Vector3f normal = new Vector3f(fRight.x - fLeft.x, fRight.y - fLeft.y, fRight.z - fLeft.z).normalize();
            consumer.vertex(m, fLeft.x,  fLeft.y,  fLeft.z)
                    .color(color)
                    .normal(entry, normal.x, normal.y, normal.z)
                    .lineWidth(1.0f);
            consumer.vertex(m, fRight.x, fRight.y, fRight.z)
                    .color(color)
                    .normal(entry, normal.x, normal.y, normal.z)
                    .lineWidth(1.0f);
        });

        queue.submitCustom(matrixStack, DEBUG_LINE_LAYER, (entry, consumer) -> {
            Matrix4f m = entry.getPositionMatrix();
            // For each pair of vertices, the normal is the direction from point A to point B
            Vector3f normal = new Vector3f(fRight.x - fLeft.x, fRight.y - fLeft.y, fRight.z - fLeft.z).normalize();
            consumer.vertex(m, fLeft.x,  fLeft.y,  fLeft.z)
                    .color(color)
                    .normal(entry, normal.x, normal.y, normal.z)
                    .lineWidth(1.0f);
            consumer.vertex(m, fRight.x, fRight.y, fRight.z)
                    .color(color)
                    .normal(entry, normal.x, normal.y, normal.z)
                    .lineWidth(1.0f);
        });

        queue.submitCustom(matrixStack, DEBUG_LINE_LAYER, (entry, consumer) -> {
            Matrix4f m = entry.getPositionMatrix();
            // For each pair of vertices, the normal is the direction from point A to point B
            Vector3f normal = new Vector3f(fRight.x - fLeft.x, fRight.y - fLeft.y, fRight.z - fLeft.z).normalize();
            consumer.vertex(m, fLeft.x,  fLeft.y,  fLeft.z)
                    .color(color)
                    .normal(entry, normal.x, normal.y, normal.z)
                    .lineWidth(1.0f);
            consumer.vertex(m, fRight.x, fRight.y, fRight.z)
                    .color(color)
                    .normal(entry, normal.x, normal.y, normal.z)
                    .lineWidth(1.0f);
        });

        queue.submitCustom(matrixStack, DEBUG_LINE_LAYER, (entry, consumer) -> {
            Matrix4f m = entry.getPositionMatrix();
            // For each pair of vertices, the normal is the direction from point A to point B
            Vector3f normal = new Vector3f(fRight.x - fLeft.x, fRight.y - fLeft.y, fRight.z - fLeft.z).normalize();
            consumer.vertex(m, fLeft.x,  fLeft.y,  fLeft.z)
                    .color(color)
                    .normal(entry, normal.x, normal.y, normal.z)
                    .lineWidth(1.0f);
            consumer.vertex(m, fRight.x, fRight.y, fRight.z)
                    .color(color)
                    .normal(entry, normal.x, normal.y, normal.z)
                    .lineWidth(1.0f);
        });

        queue.submitCustom(matrixStack, DEBUG_LINE_LAYER, (entry, consumer) -> {
            Matrix4f m = entry.getPositionMatrix();
            // For each pair of vertices, the normal is the direction from point A to point B
            Vector3f normal = new Vector3f(fRight.x - fLeft.x, fRight.y - fLeft.y, fRight.z - fLeft.z).normalize();
            consumer.vertex(m, fLeft.x,  fLeft.y,  fLeft.z)
                    .color(color)
                    .normal(entry, normal.x, normal.y, normal.z)
                    .lineWidth(1.0f);
            consumer.vertex(m, fRight.x, fRight.y, fRight.z)
                    .color(color)
                    .normal(entry, normal.x, normal.y, normal.z)
                    .lineWidth(1.0f);
        });
    }

    public void renderPole(MatrixStack matrixStack, Camera camera, Vec3d pos, int color, RenderCommandQueue queue) {
        Vector3f fPos = pos.subtract(camera.getCameraPos()).toVector3f();
        int colorFaded = color & 0xFFFFFF00;

        queue.submitCustom(matrixStack, DEBUG_LINE_LAYER, (entry, consumer) -> {
            Matrix4f m = entry.getPositionMatrix();
            // Pole is vertical, so normal is (0, 1, 0)
            consumer.vertex(m, fPos.x, fPos.y - 2, fPos.z).color(colorFaded).normal(entry, 0, 1, 0).lineWidth(1.0f);
            consumer.vertex(m, fPos.x, fPos.y + 2, fPos.z).color(color).normal(entry, 0, 1, 0).lineWidth(1.0f);
        });
    }

    public void renderLine(MatrixStack matrixStack, Camera camera, Vec3d pos1, Vec3d pos2, int color, RenderCommandQueue queue) {
        Vector3f fPos1 = pos1.subtract(camera.getCameraPos()).toVector3f();
        Vector3f fPos2 = pos2.subtract(camera.getCameraPos()).toVector3f();

        queue.submitCustom(matrixStack, DEBUG_LINE_LAYER, (entry, consumer) -> {
            Matrix4f m = entry.getPositionMatrix();
            Vector3f normal = new Vector3f(fPos2.x - fPos1.x, fPos2.y - fPos1.y, fPos2.z - fPos1.z).normalize();
            consumer.vertex(m, fPos1.x, fPos1.y, fPos1.z).color(color & 0xFFFFFF00).normal(entry, normal.x, normal.y, normal.z).lineWidth(1.0f);
            consumer.vertex(m, fPos2.x, fPos2.y, fPos2.z).color(color).normal(entry, normal.x, normal.y, normal.z).lineWidth(1.0f);
        });
    }

    public void onRender(MatrixStack matrixStack, Camera camera, RenderCommandQueue queue) {
        TrackBuilder trackBuilder = SWRC.getTrackBuilder();
        if (trackBuilder == null) return;

        ArrayList<Checkpoint> checkpoints = trackBuilder.getCheckpoints();
        ArrayList<Trap> traps = trackBuilder.getTraps();

        for (Checkpoint checkpoint : checkpoints) {
            renderCheckpoint(matrixStack, camera, checkpoint.getLeft(), checkpoint.getRight(), 0xFFFFFFFF, queue);
        }

        if (trackBuilder.checkpointBuilder.hasActiveCheckpoint()) {
            Checkpoint active = trackBuilder.checkpointBuilder.getActiveCheckpoint();
            if (active.isValid()) {
                renderCheckpoint(matrixStack, camera, active.getLeft(), active.getRight(), 0xFF55FF55, queue);
            } else {
                if (active.getLeft() != null)
                    renderPole(matrixStack, camera, active.getLeft(), 0xFF55FF55, queue);
                if (active.getRight() != null)
                    renderPole(matrixStack, camera, active.getRight(), 0xFF55FF55, queue);
            }
        }

        for (Trap trap : traps) {
            renderCheckpoint(matrixStack, camera, trap.enter.getLeft(), trap.enter.getRight(), 0xFFFF5555, queue);
            renderCheckpoint(matrixStack, camera, trap.exit.getLeft(),  trap.exit.getRight(),  0xFFFF5555, queue);
            renderLine(matrixStack, camera, trap.enter.getCenter(), trap.exit.getCenter(), 0xFFFF5555, queue);
        }

        if (trackBuilder.trapBuilder.hasActiveTrap()) {
            Trap activeTrap = trackBuilder.trapBuilder.getActiveTrap();
            if (activeTrap.enter != null)
                renderCheckpoint(matrixStack, camera, activeTrap.enter.getLeft(), activeTrap.enter.getRight(), 0xFFFF5555, queue);
            if (activeTrap.exit != null)
                renderCheckpoint(matrixStack, camera, activeTrap.exit.getLeft(), activeTrap.exit.getRight(), 0xFFFF5555, queue);
            if (activeTrap.isValid())
                renderLine(matrixStack, camera, activeTrap.enter.getCenter(), activeTrap.exit.getCenter(), 0xFFFF5555, queue);
        }

        if (trackBuilder.hasPit()) {
            Checkpoint pit = trackBuilder.getPit();
            renderCheckpoint(matrixStack, camera, pit.getLeft(), pit.getRight(), 0xFF5555FF, queue);
        }

        if (trackBuilder.hasPitEnter()) {
            Checkpoint pitEnter = trackBuilder.getPitEnter();
            renderCheckpoint(matrixStack, camera, pitEnter.getLeft(), pitEnter.getRight(), 0xFF55FFFF, queue);
        }
    }
}