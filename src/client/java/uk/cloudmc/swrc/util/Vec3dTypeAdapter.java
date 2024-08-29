package uk.cloudmc.swrc.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;

public class Vec3dTypeAdapter extends TypeAdapter<Vec3d> {
    @Override
    public void write(JsonWriter out, Vec3d src) throws IOException {
        out.beginArray();
        out.value(src.x);
        out.value(src.y);
        out.value(src.z);
        out.endArray();
    }

    @Override
    public Vec3d read(JsonReader src) throws IOException {
        src.beginArray();

        double x = src.nextDouble();
        double y = src.nextDouble();
        double z = src.nextDouble();

        src.endArray();

        return new Vec3d(x, y, z);
    }
}