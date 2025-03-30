package uk.cloudmc.swrc.net.packets;

import com.google.gson.annotations.Expose;
import uk.cloudmc.swrc.track.Track;

import java.nio.charset.StandardCharsets;

public class C2SDebugEvalPacket extends Packet<C2SDebugEvalPacket> {
    public static final char packetId = 0x13;

    @Override
    public String toString() {
        return "C2SDebugEvalPacket{" +
                "payload='" + payload + '\'' +
                '}';
    }

    @Expose
    public String payload;

    @Override
    public char getPacketId() {
        return packetId;
    }

    @Override
    public C2SDebugEvalPacket fromBytes(byte[] data) {
        return Track.gsonSerializer.fromJson(new String(data, StandardCharsets.UTF_8), C2SDebugEvalPacket.class);
    }

    @Override
    public byte[] serialize() {
        return Track.gsonSerializer.toJson(this).getBytes();
    }
}
