package uk.cloudmc.swrc.net.packets;

import com.google.gson.annotations.Expose;
import uk.cloudmc.swrc.track.Track;

import java.nio.charset.StandardCharsets;

public class C2SPopFlapPacket extends Packet<C2SPopFlapPacket> {
    public static final char packetId = 0x15;

    @Override
    public char getPacketId() {
        return packetId;
    }

    @Override
    public C2SPopFlapPacket fromBytes(byte[] data) {
        return Track.gsonSerializer.fromJson(new String(data, StandardCharsets.UTF_8), C2SPopFlapPacket.class);
    }

    @Override
    public byte[] serialize() {
        return Track.gsonSerializer.toJson(this).getBytes();
    }
}
