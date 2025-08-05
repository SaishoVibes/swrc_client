package uk.cloudmc.swrc.net.packets;

import com.google.gson.annotations.Expose;
import uk.cloudmc.swrc.track.Track;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class C2SReorderPacket extends Packet<C2SReorderPacket> {
    public static final char packetId = 0x16;

    public C2SReorderPacket(List<String> order) {
        this.order = order;
    }

    @Expose public List<String> order;

    @Override
    public char getPacketId() {
        return packetId;
    }

    @Override
    public C2SReorderPacket fromBytes(byte[] data) {
        return Track.gsonSerializer.fromJson(new String(data, StandardCharsets.UTF_8), C2SReorderPacket.class);
    }

    @Override
    public byte[] serialize() {
        return Track.gsonSerializer.toJson(this).getBytes();
    }
}
