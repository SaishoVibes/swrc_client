package uk.cloudmc.swrc;

import org.apache.commons.codec.digest.DigestUtils;
import uk.cloudmc.swrc.net.RCWebsocketConnection;
import uk.cloudmc.swrc.net.RacerWebsocketConnection;

import java.net.URI;
import java.util.Calendar;

public class WebsocketManager {
    public static RCWebsocketConnection rcWebsocketConnection;
    public static RacerWebsocketConnection racerWebsocketConnection;

    private static String generateHeadToken() {
        return DigestUtils.sha1Hex(String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));
    }

    public static void connectWebsocket(URI uri) {
        if (racerWebsocketConnection != null && racerWebsocketConnection.isOpen())  {
            racerWebsocketConnection.close();
            racerWebsocketConnection = null;
        }

        String head_token = generateHeadToken();

        racerWebsocketConnection = new RacerWebsocketConnection(uri.resolve("racer?token=" + head_token));
        racerWebsocketConnection.connect();

        if (SWRCConfig.getInstance().rc_key.length() > 0) {
            if (rcWebsocketConnection != null && rcWebsocketConnection.isOpen()) {
                rcWebsocketConnection.close();
                rcWebsocketConnection = null;
            }

            rcWebsocketConnection = new RCWebsocketConnection(uri.resolve(String.format("racecontrol?token=%s&api_key=%s", head_token, SWRCConfig.getInstance().rc_key)));
            rcWebsocketConnection.connect();
        }
    }

    public static boolean racerSocketAvalible() {
        return racerWebsocketConnection != null && racerWebsocketConnection.isOpen();
    }
    public static boolean rcSocketAvalible() {
        return rcWebsocketConnection != null && rcWebsocketConnection.isOpen();
    }

}
