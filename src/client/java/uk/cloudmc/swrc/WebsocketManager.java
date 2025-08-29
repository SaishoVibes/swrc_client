package uk.cloudmc.swrc;

import org.apache.commons.codec.digest.DigestUtils;
import uk.cloudmc.swrc.net.RCWebsocketConnection;
import uk.cloudmc.swrc.net.RacerWebsocketConnection;
import uk.cloudmc.swrc.net.SWRCWebsocketConnection;

import java.net.URI;
import java.util.Calendar;

public class WebsocketManager {
    public static RCWebsocketConnection rcWebsocketConnection;
    public static RacerWebsocketConnection racerWebsocketConnection;
    public static SWRCWebsocketConnection swrcWebsocketConnection;


    private static String generateHeadToken() {
        return DigestUtils.sha1Hex(String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));
    }

    public static void connect(URI uri) {
        if (swrcWebsocketConnection != null && swrcWebsocketConnection.isOpen()) {
            swrcWebsocketConnection.close();
        }

        swrcWebsocketConnection = new SWRCWebsocketConnection(uri.resolve(String.format(
                "?auth=%s&head=%s",
                SWRCConfig.getInstance().swrc_key,
                generateHeadToken()
        )));
        swrcWebsocketConnection.connect();
    }

    public static void connectSession(String session) {
        assert swrcSocketAvalible();

        URI uri = swrcWebsocketConnection.getURI();
        if (racerWebsocketConnection != null && racerWebsocketConnection.isOpen())  {
            racerWebsocketConnection.close();
            racerWebsocketConnection = null;
        }

        racerWebsocketConnection = new RacerWebsocketConnection(uri.resolve(String.format(
                "%s/racer?head=%s",
                session,
                generateHeadToken()
        )));
        racerWebsocketConnection.connect();

        if (!SWRCConfig.getInstance().race_key.isEmpty()) {
            if (rcWebsocketConnection != null && rcWebsocketConnection.isOpen()) {
                rcWebsocketConnection.close();
                rcWebsocketConnection = null;
            }

            rcWebsocketConnection = new RCWebsocketConnection(uri.resolve(String.format(
                    "%s/rc?auth=%s&head=%s",
                    session,
                    SWRCConfig.getInstance().race_key,
                    generateHeadToken()
            )));
            rcWebsocketConnection.connect();
        }
    }

    public static boolean racerSocketAvalible() {
        return racerWebsocketConnection != null && racerWebsocketConnection.isOpen();
    }

    public static boolean rcSocketAvalible() {
        return rcWebsocketConnection != null && rcWebsocketConnection.isOpen();
    }

    public static boolean swrcSocketAvalible() {
        return swrcWebsocketConnection != null && swrcWebsocketConnection.isOpen();
    }
}
