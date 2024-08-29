package uk.cloudmc.swrc.track;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import net.minecraft.util.math.Vec3d;
import uk.cloudmc.swrc.util.Checkpoint;
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
    public final String name;
    @Expose
    public final long minimumLapTime;

    public Track(String id, String name, long minimumLapTime, ArrayList<Checkpoint> checkpoints) {
        this.id = id;
        this.name = name;
        this.minimumLapTime = minimumLapTime;
        this.checkpoints = checkpoints;
    }

    public String serialize() {
        return gsonSerializer.toJson(this);
    }

    public static Track deserialize(String src) { return gsonSerializer.fromJson(src, Track.class); }
}
