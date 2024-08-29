package uk.cloudmc.swrc.net.packets;

import org.apache.commons.lang3.NotImplementedException;
import uk.cloudmc.swrc.SWRC;

import java.nio.ByteBuffer;

public abstract class Packet<T extends Packet<T>> {
    public static final char packetId = 0xFF;

    public T fromBytes(byte[] data) {
        throw new NotImplementedException();
    }

    public Packet() {}

    @Override
    public String toString() {
        return "Packet{}";
    }

    public byte[] serialize() { throw new NotImplementedException(); }

    public char getPacketId() { return packetId; }

    public ByteBuffer serializeForNetwork() {
        char packetId = getPacketId();
        byte[] bytes = serialize();

        ByteBuffer packet = ByteBuffer.allocate(bytes.length + 2); // Null byte first
        packet.putChar(packetId);
        packet.put(bytes);

        return packet;
    }
}
