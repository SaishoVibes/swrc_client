package uk.cloudmc.swrc.util;

import com.google.gson.annotations.Expose;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;

public class Checkpoint {

    @Expose
    private Vec3d left;
    @Expose
    private Vec3d right;

    private double lineLength;
    private Vec3d center;

    HashMap<AbstractClientPlayerEntity, Boolean> checkpoint_sides = new HashMap<>();

    public Checkpoint() {}

    public ArrayList<AbstractClientPlayerEntity> getLineCrosses(ArrayList<PositionSnapshot> positionSnapshots) {
        ArrayList<AbstractClientPlayerEntity> line_crosses = new ArrayList<>();

        for (PositionSnapshot positionSnapshot : positionSnapshots) {
            if (!checkpoint_sides.containsKey(positionSnapshot.getPlayer())) continue;

            boolean side = getSide(positionSnapshot.getPosition());
            boolean last_side = checkpoint_sides.get(positionSnapshot.getPlayer());

            if (side != last_side) {
                if (side) {
                    line_crosses.add(positionSnapshot.getPlayer());
                }

                checkpoint_sides.put(positionSnapshot.getPlayer(), side);
            }
        }

        return line_crosses;
    }

    public boolean getSide(Vec3d position) {
        if (left == null || right == null) {
            throw new RuntimeException("Left or Right not set on checkpoint");
        }

        double between_factor = left.distanceTo(position) - right.distanceTo(position);
        boolean between = between_factor > (lineLength - 1) && between_factor > (-lineLength + 1);

        if (left.getX() > right.getX()) {
            return (position.getZ()-left.getZ()) > safeDivide(left.getZ()-right.getZ(), left.getX()-right.getX()) * (position.getX()-left.getX()) && between;
        } else {
            return (position.getZ()-left.getZ()) < safeDivide(left.getZ()-right.getZ(), left.getX()-right.getX()) * (position.getX()-left.getX()) && between;
        }
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

    private void recalculate() {
        if (left == null || right == null) return;

        lineLength = left.distanceTo(right);
        center = left.add(right).multiply(0.5d);
    }

    private double safeDivide(double x, double y) {
        if (x == 0 || y == 0) {
            return 0;
        }

        return x / y;
    }
}
