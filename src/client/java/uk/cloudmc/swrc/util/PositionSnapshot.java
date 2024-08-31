package uk.cloudmc.swrc.util;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class PositionSnapshot {

    private final Vec3d position;
    private final String player;

    public PositionSnapshot(String player, Vec3d position) {
        this.position = position;
        this.player = player;
    }

    public Vec3d getPosition() {
        return position;
    }

    public String getPlayer() {
        return player;
    }
}
