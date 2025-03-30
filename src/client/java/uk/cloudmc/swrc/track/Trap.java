package uk.cloudmc.swrc.track;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import net.minecraft.util.math.Vec3d;
import uk.cloudmc.swrc.util.Vec3dTypeAdapter;

public class Trap {
    public static final Gson gsonSerializer = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(Vec3d.class, new Vec3dTypeAdapter())
            .setPrettyPrinting()
            .create();

    @Expose
    public Checkpoint enter;
    @Expose
    public Checkpoint exit;

    public Trap() {
    }

    public boolean isValid() {
        return enter != null && exit != null;
    }
}
