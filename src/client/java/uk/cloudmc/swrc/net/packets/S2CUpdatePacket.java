package uk.cloudmc.swrc.net.packets;

import com.google.gson.annotations.Expose;
import uk.cloudmc.swrc.track.Track;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class S2CUpdatePacket extends Packet<S2CUpdatePacket> {
    public static final char packetId = 0x05;

    @Expose
    public ArrayList<String> racers;

    @Override
    public String toString() {
        return "S2CUpdatePacket{" +
                "racers=" + racers +
                '}';
    }

    @Override
    public char getPacketId() {
        return packetId;
    }

    @Override
    public S2CUpdatePacket fromBytes(byte[] data) {
        return Track.gsonSerializer.fromJson(new String(data, StandardCharsets.UTF_8), S2CUpdatePacket.class);
    }

    @Override
    public byte[] serialize() {
        return Track.gsonSerializer.toJson(this).getBytes();
    }
}
