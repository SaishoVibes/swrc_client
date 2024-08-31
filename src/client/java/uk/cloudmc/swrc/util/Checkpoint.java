package uk.cloudmc.swrc.util;

import com.google.gson.annotations.Expose;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.ai.brain.task.RamImpactTask;
import net.minecraft.util.math.Vec3d;
import org.slf4j.spi.LocationAwareLogger;
import uk.cloudmc.swrc.SWRC;

import java.util.ArrayList;
import java.util.HashMap;

public class Checkpoint {

    @Expose
    private Vec3d left;
    @Expose
    private Vec3d right;

    private double lineLength;
    private Vec3d center;

    HashMap<String, Boolean> checkpoint_sides = new HashMap<>();

    private class SideResult {
        public final boolean line;
        public final boolean between;

        public SideResult(boolean line, boolean between) {
            this.line = line;
            this.between = between;
        }
    }

    public Checkpoint() {}

    public ArrayList<String> getLineCrosses(ArrayList<PositionSnapshot> positionSnapshots) {
        ArrayList<String> line_crosses = new ArrayList<>();

        for (PositionSnapshot positionSnapshot : positionSnapshots) {
            SideResult side = getSide(positionSnapshot.getPosition());

            if (!checkpoint_sides.containsKey(positionSnapshot.getPlayer())) {
                checkpoint_sides.put(positionSnapshot.getPlayer(), side.line);
            }

            boolean last_side = checkpoint_sides.get(positionSnapshot.getPlayer());

            if (side.line != last_side) {
                if (side.line && side.between) {
                    line_crosses.add(positionSnapshot.getPlayer());

                    checkpoint_sides.put(positionSnapshot.getPlayer(), true);
                    continue;
                }

                checkpoint_sides.put(positionSnapshot.getPlayer(), false);
            }
        }

        return line_crosses;
    }

    public SideResult getSide(Vec3d position) {
        if (left == null || right == null) {
            throw new RuntimeException("Left or Right not set on checkpoint");
        }

        double between_factor = Math.sqrt(
                Math.pow(left.getX() - position.getX(), 2)
                + Math.pow(left.getZ() - position.getZ(), 2)
        ) - Math.sqrt(
                Math.pow(right.getX() - position.getX(), 2)
                + Math.pow(right.getZ() - position.getZ(), 2)
        );

        boolean between = Math.abs(between_factor) < lineLength - 1 && position.isInRange(this.center, lineLength / 1.5);
        boolean line;

        if (left.getX() > right.getX()) {
            line = (position.getZ()-left.getZ()) > safeDivide(left.getZ()-right.getZ(), left.getX()-right.getX()) * (position.getX()-left.getX()) && between;
        } else {
            line = (position.getZ()-left.getZ()) < safeDivide(left.getZ()-right.getZ(), left.getX()-right.getX()) * (position.getX()-left.getX()) && between;
        }

        return new SideResult(line, between);
    }

    public boolean isValid() {
        return left != null && right != null;
    }

    public Vec3d getCenter() {
        return left.add(right).multiply(0.5);
    }

    public Vec3d getLeft() {
        return left;
    }

    public void setLeft(Vec3d left) {
        this.left = left;
        recalculate();
    }

    public Vec3d getRight() {
        return right;
    }


    public void setRight(Vec3d right) {
        this.right = right;
        recalculate();
    }

    public void recalculate() {
        if (left == null || right == null) return;

        lineLength = Math.sqrt(
                Math.pow(left.getX() - right.getX(), 2)
                + Math.pow(left.getZ() - right.getZ(), 2)
        );
        center = left.add(right).multiply(0.5d);
    }

    private double safeDivide(double x, double y) {
        if (x == 0 || y == 0) {
            return 0;
        }

        return x / y;
    }
}
