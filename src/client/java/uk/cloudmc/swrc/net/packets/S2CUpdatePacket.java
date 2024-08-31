package uk.cloudmc.swrc.net.packets;

import com.google.gson.annotations.Expose;
import uk.cloudmc.swrc.track.Track;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class S2CUpdatePacket extends Packet<S2CUpdatePacket> {
    public static final char packetId = 0x05;

    public class RaceLeaderboardPosition {
        @Expose
        public String player_name;
        @Expose
        public long time_delta;

        public RaceLeaderboardPosition(String player_name, long time_delta) {
            this.player_name = player_name;
            this.time_delta = time_delta;
        }

        @Override
        public String toString() {
            return "RaceLeaderboardPosition{" +
                    "player_name='" + player_name + '\'' +
                    ", time_delta=" + time_delta +
                    '}';
        }
    }

    @Expose
    public ArrayList<String> racers;
    @Expose
    public ArrayList<RaceLeaderboardPosition> race_leaderboard;
    @Expose
    public ArrayList<RaceLeaderboardPosition> race_lap_begin;

    @Override
    public String toString() {
        return "S2CUpdatePacket{" +
                "racers=" + racers +
                ", race_leaderboard=" + race_leaderboard +
                ", race_lap_begin=" + race_lap_begin +
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
