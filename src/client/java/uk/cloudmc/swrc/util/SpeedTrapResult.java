package uk.cloudmc.swrc.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import net.minecraft.util.math.Vec3d;

public class SpeedTrapResult {
    public static final Gson gsonSerializer = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(Vec3d.class, new Vec3dTypeAdapter())
            .setPrettyPrinting()
            .create();

    @Expose
    private String player;
    @Expose
    private SnapshotTime enter;
    @Expose
    private SnapshotTime exit;

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public SnapshotTime getEnter() {
        return enter;
    }

    public void setEnter(SnapshotTime enter) {
        this.enter = enter;
    }

    public SnapshotTime getExit() {
        return exit;
    }

    public void setExit(SnapshotTime exit) {
        this.exit = exit;
    }
}
