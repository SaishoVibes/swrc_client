package uk.cloudmc.swrc.net.packets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class C2SCreateNewSessionPacket extends Packet<C2SCreateNewSessionPacket> {
    public static final char packetId = 0x40;

    private final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    @Expose public String key;

    @Override
    public String toString() {
        return "C2SCreateNewSessionPacket{" +
                "gson=" + gson +
                ", key='" + key + '\'' +
                '}';
    }

    @Override
    public char getPacketId() {
        return packetId;
    }

    @Override
    public C2SCreateNewSessionPacket fromBytes(byte[] data) {
        return gson.fromJson(new String(data, StandardCharsets.UTF_8), C2SCreateNewSessionPacket.class);
    }

    @Override
    public byte[] serialize() {
        return gson.toJson(this).getBytes();
    }
}
