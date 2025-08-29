package uk.cloudmc.swrc.net.packets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.nio.charset.StandardCharsets;

public class S2CHelloPacket extends Packet<S2CHelloPacket> {
    public static final char packetId = 0x00;

    private final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    @Expose public String server_label;

    @Override
    public String toString() {
        return "S2CHelloPacket{" +
                '}';
    }

    @Override
    public char getPacketId() {
        return packetId;
    }

    @Override
    public S2CHelloPacket fromBytes(byte[] data) {
        return gson.fromJson(new String(data, StandardCharsets.UTF_8), S2CHelloPacket.class);
    }

    @Override
    public byte[] serialize() {
        return new byte[] {};
    }
}
