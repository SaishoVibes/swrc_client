package uk.cloudmc.swrc.net.packets;

import uk.cloudmc.swrc.track.Track;

import java.nio.charset.StandardCharsets;

public class C2SEndRacePacket extends Packet<C2SEndRacePacket> {
    public static final char packetId = 0x11;

    @Override
    public String toString() {
        return "S2CEndRacePacket{" +
                '}';
    }

    @Override
    public char getPacketId() {
        return packetId;
    }

    @Override
    public C2SEndRacePacket fromBytes(byte[] data) {
        return Track.gsonSerializer.fromJson(new String(data, StandardCharsets.UTF_8), C2SEndRacePacket.class);
    }

    @Override
    public byte[] serialize() {
        return Track.gsonSerializer.toJson(this).getBytes();
    }
}
