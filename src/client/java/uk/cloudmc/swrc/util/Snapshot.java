package uk.cloudmc.swrc.util;

import com.google.gson.annotations.Expose;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.Vec3d;

public class Snapshot {
    @Expose
    private final Vec3d position;
    @Expose
    private final Vec3d velocity;
    @Expose
    private final String player;

    public Snapshot(String player, Vec3d position, Vec3d velocity) {
        this.position = position;
        this.velocity = velocity;
        this.player = player;
    }

    public Vec3d getPosition() {
        return position;
    }

    public Vec3d getVelocity() {
        return velocity;
    }

    public String getPlayer() {
        return player;
    }
}
