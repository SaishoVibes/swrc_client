package uk.cloudmc.swrc.track;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import net.minecraft.util.math.Vec3d;
import uk.cloudmc.swrc.util.Vec3dTypeAdapter;

import java.util.ArrayList;

public class Track {
    public static final Gson gsonSerializer = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(Vec3d.class, new Vec3dTypeAdapter())
            .setPrettyPrinting()
            .create();

    @Expose
    public final String id;
    @Expose
    public final ArrayList<Checkpoint> checkpoints;
    @Expose
    public final ArrayList<Trap> traps;
    @Expose
    public final String name;
    @Expose
    public final long minimumLapTime;
    @Expose
    public Checkpoint pit;
    @Expose
    public Checkpoint pit_enter;
    @Expose
    public final boolean pitCountsAsLap;

    public Track(String id, String name, long minimumLapTime, ArrayList<Checkpoint> checkpoints, ArrayList<Trap> traps, boolean pitCountsAsLap) {
        this.id = id;
        this.traps = traps;
        this.name = name;
        this.minimumLapTime = minimumLapTime;
        this.checkpoints = checkpoints;
        this.pitCountsAsLap = pitCountsAsLap;

        for (Checkpoint checkpoint : this.checkpoints) {
            checkpoint.recalculate();
        }

        for (Trap trap: this.traps) {
            trap.enter.recalculate();
            trap.exit.recalculate();
        }

        if (this.pit != null) {
            this.pit.recalculate();
        }

        if (this.pit_enter != null) {
            this.pit_enter.recalculate();
        }
    }

    public String serialize() {
        return gsonSerializer.toJson(this);
    }

    public static Track deserialize(String src) {
        Track track = gsonSerializer.fromJson(src, Track.class);

        for (Checkpoint checkpoint : track.checkpoints) {
            checkpoint.recalculate();
        }

        if (track.traps != null) {
            for (Trap trap: track.traps) {
                trap.enter.recalculate();
                trap.exit.recalculate();
            }
        }

        if (track.pit != null) {
            track.pit.recalculate();
        }

        if (track.pit_enter != null) {
            track.pit_enter.recalculate();
        }

        return track;
    }
}
