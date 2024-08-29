package uk.cloudmc.swrc.net;

import uk.cloudmc.swrc.Race;
import uk.cloudmc.swrc.SWRC;
import uk.cloudmc.swrc.net.packets.*;
import uk.cloudmc.swrc.util.ChatFormatter;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

public class RacerWebsocketConnection extends AbstractWebsocketConnection {
    public RacerWebsocketConnection(URI uri) { super(uri); }

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
            case(S2CNewRacePacket.packetId):
                onPacket(new S2CNewRacePacket().fromBytes(payload));
                break;
            case(S2CUpdatePacket.packetId):
                onPacket(new S2CUpdatePacket().fromBytes(payload));
                break;
            default:
                SWRC.LOGGER.info(String.format("Got unknown packet id %s", packetId));
        }
    }

    @Override
    public void onConnect() {

    }

    @Override
    public void onPacket(Packet uPacket) {
        if (uPacket instanceof S2CHelloPacket) {
            SWRC.instance.inGameHud.getChatHud().addMessage(ChatFormatter.GENERIC_MESSAGE("[Racer] Successfully connected to server."));

            C2SHandshakePacket handshake = new C2SHandshakePacket();

            handshake.username = SWRC.instance.player.getName().getString();
            handshake.uuid = SWRC.instance.player.getUuidAsString();
            handshake.version = SWRC.VERSION;

            sendPacket(handshake);
        }
        if (uPacket instanceof S2CHandshakePacket) {
            SWRC.instance.inGameHud.getChatHud().addMessage(ChatFormatter.GENERIC_MESSAGE("[Racer] Authenticated."));
        }
        if (uPacket instanceof S2CNewRacePacket) {
            S2CNewRacePacket packet = (S2CNewRacePacket) uPacket;

            SWRC.setRace(new Race(packet.race_id, packet.track));

            SWRC.instance.inGameHud.getChatHud().addMessage(ChatFormatter.GENERIC_MESSAGE(String.format("[Racer] Received new race from server (%s)", packet.race_id)));
        }
        if (uPacket instanceof S2CUpdatePacket) {
            S2CUpdatePacket packet = (S2CUpdatePacket) uPacket;
            SWRC.LOGGER.info(packet.racers.toString());
        }
    }
}
