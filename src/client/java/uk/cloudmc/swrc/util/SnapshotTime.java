package uk.cloudmc.swrc.util;

import com.google.gson.annotations.Expose;
import net.minecraft.util.math.Vec3d;

public class SnapshotTime extends Snapshot {

    @Expose
    private final long timestamp;

    public SnapshotTime(String player, Vec3d position, Vec3d velocity, long timestamp) {
        super(player, position, velocity);
        this.timestamp = timestamp;
    }

    public SnapshotTime(Snapshot snapshot, long timestamp) {
        super(snapshot.getPlayer(), snapshot.getPosition(), snapshot.getVelocity());
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }
}