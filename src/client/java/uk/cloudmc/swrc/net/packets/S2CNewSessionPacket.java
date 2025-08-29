package uk.cloudmc.swrc.net.packets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class S2CNewSessionPacket extends Packet<S2CNewSessionPacket> {
    public static final char packetId = 0x41;

    private final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    @Expose public String id;
    @Expose public String race_key;

    @Override
    public String toString() {
        return "S2CNewSessionPacket{" +
                "gson=" + gson +
                ", id='" + id + '\'' +
                ", race_key='" + race_key + '\'' +
                '}';
    }

    @Override
    public char getPacketId() {
        return packetId;
    }

    @Override
    public S2CNewSessionPacket fromBytes(byte[] data) {
        return gson.fromJson(new String(data, StandardCharsets.UTF_8), S2CNewSessionPacket.class);
    }

    @Override
    public byte[] serialize() {
        return new byte[] {};
    }
}
