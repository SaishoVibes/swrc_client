package uk.cloudmc.swrc.net;

import uk.cloudmc.swrc.Race;
import uk.cloudmc.swrc.SWRC;
import uk.cloudmc.swrc.net.packets.*;
import uk.cloudmc.swrc.util.ChatFormatter;

import java.net.URI;
import java.util.Arrays;

public class RCWebsocketConnection extends AbstractWebsocketConnection {
    public RCWebsocketConnection(URI uri) {
        super(uri);
    }

    @Override
    public void onMessage(String message) {
        byte[] bytes = message.getBytes();

        int packetId = bytes[0];
        byte[] payload = Arrays.copyOfRange(bytes, 1, bytes.length);

        switch (packetId) {
            case(S2CHelloPacket.packetId):
                onPacket(new S2CHelloPacket().fromBytes(payload));
                break;
            case(S2CHandshakePacket.packetId):
                onPacket(new S2CHandshakePacket().fromBytes(payload));
                break;
            default:
                SWRC.LOGGER.warn(String.format("Got unknown packet id %s", packetId));
        }
    }

    @Override
    public void onConnect() {

    }

    @Override
    public void onPacket(Packet uPacket) {
        if (uPacket instanceof S2CHelloPacket) {
            SWRC.instance.inGameHud.getChatHud().addMessage(ChatFormatter.GENERIC_MESSAGE("[RC] Successfully connected to server."));

            C2SHandshakePacket handshake = new C2SHandshakePacket();

            handshake.username = SWRC.instance.player.getName().getString();
            handshake.uuid = SWRC.instance.player.getUuidAsString();
            handshake.version = SWRC.VERSION;

            sendPacket(handshake);
        }
        if (uPacket instanceof S2CHandshakePacket) {
            SWRC.instance.inGameHud.getChatHud().addMessage(ChatFormatter.GENERIC_MESSAGE("[RC] Authenticated as Race Control."));
        }
    }
}
